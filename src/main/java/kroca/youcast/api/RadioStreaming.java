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

import java.util.*;

/**
 * Сама примитивная имплементация стриминга аудио потока на основе песен, лежащих в папке загрузок.
 * Играет все загруженные на момент старта сервера песни по очереди (Делать онлайн обновление не инетересно)
 */
public class RadioStreaming implements Handler<RoutingContext> {

    private Logger logger = LoggerFactory.getLogger(RadioStreaming.class);
    private final Vertx vertx;
    private List<String> playList;
    private final Set<HttpServerResponse> activeListeners = new HashSet<>();
    private AsyncFile currentlyPlaying;
    private long positionInFile;
    private int currentSongNumber;

    RadioStreaming(Vertx vertx) {
        this.vertx = vertx;
        startStreaming();
    }

    @Override
    public void handle(RoutingContext context) {
        addListener(context.request());
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

    private void startStreaming() {
        playList = vertx.fileSystem().readDirBlocking("src/main/uploads");
        if (!playList.isEmpty()) {
            currentlyPlaying = openFile(playList.get(0));
            vertx.setPeriodic(100, this::streamAudioChunk);
        } else {
            logger.error("Nothing to stream. Upload songs and restart the server");
        }

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
            openNextSong();
        }
        activeListeners.forEach(listener -> {
            if (!listener.writeQueueFull()) {
                listener.write(buffer.copy());
            } else {
                logger.error("write queue is full");
            }
        });
    }

    private void openNextSong() {
        if (currentSongNumber + 1 < playList.size()) {
            currentSongNumber++;
        } else if (currentSongNumber + 1 == playList.size()) {
            currentSongNumber = 0;
        }
        openFile(playList.get(currentSongNumber));
        //reset offset
        positionInFile = 0;
    }

    private AsyncFile openFile(String fileName) {
        return vertx.fileSystem()
                .openBlocking(fileName, new OpenOptions().setRead(true));
    }
}
