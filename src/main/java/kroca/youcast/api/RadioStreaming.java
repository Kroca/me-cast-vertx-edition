package kroca.youcast.api;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class RadioStreaming implements Handler<RoutingContext> {

    private Logger logger = LoggerFactory.getLogger(RadioStreaming.class);

    //    private Queue<String> playList = new ArrayDeque<>();
    private final Set<HttpServerResponse> activeListeners = new HashSet<>();
    private AsyncFile currentlyPlaying;
    private long positionInFile;

    RadioStreaming(Vertx vertx) {
        currentlyPlaying = vertx.fileSystem()
                .openBlocking("src/main/uploads/c401f8a9-59cc-46ae-9f15-e494a79a4a9f", new OpenOptions().setRead(true));
        positionInFile = 0;
        vertx.setPeriodic(100, this::streamAudioChunk);
    }

    private void addListener(HttpServerRequest request) {
        logger.info("user joined");
        HttpServerResponse response = request.response();
        response.putHeader("Content-Type", "audio/mpeg")
                .setChunked(true);
        activeListeners.add(response);
        response.endHandler(v -> {
            logger.info("user left");
            activeListeners.remove(response);
        });
    }

    private void streamAudioChunk(long id) {
        if (currentlyPlaying != null) {
            currentlyPlaying.read(Buffer.buffer(4096), 0, positionInFile, 4096, ar -> {
                if (ar.succeeded()) {
                    processReadBuffer(ar.result());
                } else {
                    logger.error("read failed");
                }
            });
        }
    }

    private void processReadBuffer(Buffer buffer) {
        logger.info("Read {} bytes from pos {}", buffer.length(), positionInFile);
        positionInFile += buffer.length();
        if (buffer.length() == 0) {
            //reset
            positionInFile = 0;
        }
        activeListeners.forEach(listener -> {
            if (!listener.writeQueueFull()) {
                listener.write(buffer.copy());
            }
        });
    }

    @Override
    public void handle(RoutingContext context) {
        addListener(context.request());
    }
}
