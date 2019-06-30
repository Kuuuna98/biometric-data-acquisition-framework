package kr.ac.kaist.ic.kimauk.drmshowroom.dataprocessing.application.updatelog;

import kr.ac.kaist.ic.kimauk.drmshowroom.dataprocessing.domain.model.drm_data.log.LogSQLRepository;
import kr.ac.kaist.ic.kimauk.drmshowroom.dataprocessing.domain.model.drm_data.history.History;
import kr.ac.kaist.ic.kimauk.drmshowroom.dataprocessing.domain.model.drm_data.history.HistoryRepository;
import kr.ac.kaist.ic.kimauk.drmshowroom.dataprocessing.domain.model.drm_data.history.HistoryType;
import kr.ac.kaist.ic.kimauk.drmshowroom.dataprocessing.domain.service.UpdateLog;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Service
public class UpdateLogImpl implements UpdateLog {


    @Value("${upload.directory.window}")
    private String uploadFileDirectoryForWindow;

    @Value("${upload.directory.mac}")
    private String uploadFileDirectoryForMac;

    @Value("${upload.directory.unix}")
    private String uploadFileDirectoryForUbuntu;

    @Autowired
    private LogSQLRepository logSqlRepository;

    @Autowired
    private ThreadPoolTaskExecutor threadPool;

    @Autowired
    private HistoryRepository historyRepository;

    private String getUploadFileDirectory(){
        String systemType = System.getProperty("os.name").toUpperCase();
        if(systemType.contains("WINDOW"))
            return uploadFileDirectoryForWindow;
        else if(systemType.contains("LINUX"))
            return uploadFileDirectoryForUbuntu;
        else
            return uploadFileDirectoryForMac;
    }

    @Override
    public void run() {
        insertLogs();
        System.exit(0);
    }

    private void insertPhoneNumberForGuideLogs(){

    }

    private void removeDuplicateLogs(){
        String sql = "DELETE FROM Log WHERE id not in (SELECT min(id) FROM Log GROUP By type, timestamp)";
    }

    private void insertLogs(){
        Set<Path> uploadFilePathSet = new HashSet<>();
        try {
            Files.walk(new File(getUploadFileDirectory()).toPath())
                    .filter(Files::isRegularFile)
                    .filter(p -> !p.toString().contains("unzip"))
                    .forEach(uploadFilePathSet::add);
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<History> prevUploadFileHistoryList = historyRepository.findAllByIdHistoryType(HistoryType.UPLOAD_FILE_IMPORTING_DONE);
        prevUploadFileHistoryList.addAll(historyRepository.findAllByIdHistoryType(HistoryType.UPLOAD_FILE_IMPORTING_FAILED));
        for(History history : prevUploadFileHistoryList)
            uploadFilePathSet.remove(new File(history.getValue()).toPath());

        System.out.println("INSERTING "+ uploadFilePathSet.size() + " FILES");

        List<Future<SQLiteLogs>> futureList = new ArrayList<>();


        List<Path> uploadFilePathList = new ArrayList<>(uploadFilePathSet);

        Collections.sort(uploadFilePathList, (o1, o2) -> {
            String o1FileName = o1.getFileName().toString();
            String o2FileName = o2.getFileName().toString();
            return new CompareToBuilder().append(o1FileName.split("_")[1], o2FileName.split("_")[1]).toComparison();
        });

        Iterator<Path> uploadFilePathIterator = uploadFilePathList.iterator();

        for(;;){
            for(int threadNumber = threadPool.getActiveCount();
                threadNumber < threadPool.getMaxPoolSize() && uploadFilePathIterator.hasNext();
                threadNumber++)
                futureList.add(
                        threadPool.submit(
                                new LogsCreatorCallable(uploadFilePathIterator.next().toFile())
                        )
                );
            for(Future<SQLiteLogs> future: futureList) {
                try {
                    SQLiteLogs sqLiteLogs = future.get();
                    logSqlRepository.saveAll(sqLiteLogs.getLogs());
                    historyRepository.save(sqLiteLogs.getHistory());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }

            if(!uploadFilePathIterator.hasNext())
                break;
        }
    }
}
