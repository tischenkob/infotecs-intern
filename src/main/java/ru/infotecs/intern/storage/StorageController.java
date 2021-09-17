package ru.infotecs.intern.storage;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static ru.infotecs.intern.storage.StorageController.Response.Status;

@RestController
@RequestMapping("store")
@RequiredArgsConstructor
public class StorageController {

  private final StorageService service;
  @Setter
  @Value("${dump.file.name:storage_dump.txt}")
  private String dumpFileName;

  @PostMapping("/records/{key}")
  public Response createRecord(@PathVariable String key,
                               @RequestParam("value") String value,
                               @RequestParam(value = "ttl", required = false) Integer ttl
                              ) {
    if (ttl != null && ttl < 1) {
      return new Response(Status.FAILURE, "ttl must be greater than 0");
    }
    service.createRecord(key, value, ttl);
    return new Response(Status.SUCCESS, "New record was created!");
  }

  @GetMapping("/records/{key}")
  public Response readRecord(@PathVariable String key) {
    return service.readRecord(key)
                  .map(value -> new Response(Status.SUCCESS, "Record found.", value))
                  .orElseGet(() -> new Response(Status.FAILURE, "Record not found."));
  }

  @DeleteMapping("/records/{key}")
  public Response deleteRecord(@PathVariable String key) {
    return service.deleteRecord(key)
                  .isPresent() ? new Response(Status.SUCCESS, "Successfully removed.")
                               : new Response(Status.FAILURE, "Record not Found.");
  }

  @GetMapping(value = "dump")
  public void getDump(HttpServletResponse response) throws IOException {
    File file = service.createDumpFile();
    response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
    response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                       "attachment; filename=\"" + dumpFileName + "\"");
    FileCopyUtils.copy(new FileInputStream(file), response.getOutputStream());
    if (!file.delete()) {
      throw new IOException("Temporary dump file %s could not be deleted".formatted(file.getName()));
    }
  }

  @PostMapping("load")
  public Response loadDump(@RequestParam("file") MultipartFile file) {
    try {
      service.loadDump(file.getInputStream());
      return new Response(Status.SUCCESS, "File was successfully loaded.");
    } catch (IOException e) {
      return new Response(Status.FAILURE, "File could not be loaded.");
    }
  }

  @Data
  @AllArgsConstructor
  @RequiredArgsConstructor
  static class Response {
    private final Status status;
    private final String message;
    private Object data;

    enum Status {
      SUCCESS, FAILURE
    }
  }

}
