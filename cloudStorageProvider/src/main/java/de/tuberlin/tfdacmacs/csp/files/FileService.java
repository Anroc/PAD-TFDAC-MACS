package de.tuberlin.tfdacmacs.csp.files;

import de.tuberlin.tfdacmacs.csp.files.data.FileInformation;
import de.tuberlin.tfdacmacs.csp.files.db.FileInformationDB;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    private final FileInformationDB fileInformationDB;
    private final FileConfiguration fileConfiguration;

    @PostConstruct
    public void init() {
        Paths.get(fileConfiguration.getDataDir()).toFile().mkdirs();
    }

    public FileInformation saveFile(@NonNull String id, @NonNull String originalFilename, byte[] bytes) throws IOException {
        FileInformation fileInformation = createNewFile(id, originalFilename);
        log.info("Creating new file '{}' [{}]", fileInformation.getOriginalName(), Paths.get(fileInformation.getPath()).toFile().getAbsolutePath());
        File file = Files.createFile(Paths.get(fileInformation.getPath())).toFile();
        Files.write(file.toPath(), bytes);

        fileInformationDB.insert(fileInformation);
        log.info("Created file [{}]", fileInformation.getPath());
        return fileInformation;
    }

    private FileInformation createNewFile(String id, String originalFilename) {
        return new FileInformation(id, buildNewFilePath(id), originalFilename);
    }

    private String buildNewFilePath(String fileName) {
        String dataDir = fileConfiguration.getDataDir();
        if(dataDir.endsWith(File.separator)) {
            return dataDir + fileName;
        } else {
            return dataDir + File.separator + fileName;
        }
    }


    public Optional<byte[]> retrieveFile(@NonNull String id) {
        return findFileInformation(id).flatMap(this::getFile);
    }

    private Optional<byte[]> getFile(FileInformation fileInformation) {
        try {
            return Optional.of(Files.readAllBytes(Paths.get(fileInformation.getPath())));
        } catch (IOException e) {
            log.error("Could not load file [{}].", fileInformation.getPath(), e);
            return Optional.empty();
        }
    }

    public Optional<FileInformation> findFileInformation(@NonNull String id) {
        return fileInformationDB.findEntity(id);
    }
}
