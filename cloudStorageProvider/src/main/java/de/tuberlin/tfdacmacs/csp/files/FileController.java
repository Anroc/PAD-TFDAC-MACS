package de.tuberlin.tfdacmacs.csp.files;

import de.tuberlin.tfdacmacs.csp.files.data.FileInformation;
import de.tuberlin.tfdacmacs.csp.files.data.dto.FileInformationResponse;
import de.tuberlin.tfdacmacs.lib.exceptions.NotFoundException;
import de.tuberlin.tfdacmacs.lib.exceptions.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FileInformationResponse uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new ServiceException("Given file was empty.", HttpStatus.BAD_REQUEST);
        }

        try {
            FileInformation fileInformation = fileService.saveFile(file.getOriginalFilename(), file.getBytes());
            return FileInformationResponse.from(fileInformation);
        } catch (IOException e) {
            throw new ServiceException("Could not save file.", e);
        }
    }

    @GetMapping(value = "/{id}", produces = {"application/octet-stream", MediaType.APPLICATION_OCTET_STREAM_VALUE})
    public byte[] getFile(@PathVariable("id") String id) {
        return fileService.retrieveFile(id).orElseThrow(() -> new NotFoundException(id));
    }

    @GetMapping("/{id}/information")
    public FileInformationResponse getFileInformation(@PathVariable("id") String id ) {
        return fileService.findFileInformation(id)
                .map(FileInformationResponse::from)
                .orElseThrow(() -> new NotFoundException(id));
    }
}
