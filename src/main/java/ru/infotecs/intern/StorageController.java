package ru.infotecs.intern;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

import static ru.infotecs.intern.StorageController.Response.Status;

@RestController
@RequestMapping("store")
@RequiredArgsConstructor
public class StorageController {

  private final StorageService service;
  @Setter
  @Value("${dump.file.name:storage_dump.properties}")
  private String dumpFileName;

  @PostMapping("/records/{key}")
  public Response createRecord(
      @PathVariable String key,
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

  @ResponseBody
  @GetMapping(value = "dump", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  public ResponseEntity<File> getDump() throws IOException {
    File file = service.getDumpFile();
    var response = ResponseEntity.ok()
                                 .header(HttpHeaders.CONTENT_DISPOSITION,
                                         "attachment; filename=\"" + file.getName() + "\"")
                                 .body(file);
    if (!file.delete()) {
      throw new IOException("Temporary dump file %s could not be deleted".formatted(file.getName()));
    }
    return response;
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
