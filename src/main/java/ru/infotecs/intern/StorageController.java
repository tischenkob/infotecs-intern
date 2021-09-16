package ru.infotecs.intern;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.Properties;
import java.util.stream.Collectors;

import static ru.infotecs.intern.StorageController.Response.Status;

@RestController
@RequestMapping("store")
@RequiredArgsConstructor
public class StorageController {

  private final TimedStorage<String> storage;
  private final String               dumpFileName = "storage_dump.properties";

  @PostMapping("/records/{key}")
  public Response createRecord(
      @PathVariable String key,
      @RequestParam("value") String value,
      @RequestParam(value = "ttl", required = false) Integer ttl
                              ) {
    if (ttl != null && ttl < 0) {
      return new Response(Status.FAILURE, "ttl must be greater than 0");
    }

    if (ttl == null) {
      storage.put(key, value);
    } else {
      storage.put(key, value, ttl);
    }
    return new Response(Status.SUCCESS, "New record was created!");
  }

  @GetMapping("/records/{key}")
  public Response readRecord(@PathVariable String key) {
    return storage.get(key).map(value -> new Response(Status.SUCCESS, "Record found.", value))
                  .orElseGet(() -> new Response(Status.FAILURE, "Record not found."));
  }

  @DeleteMapping("/records/{key}")
  public Response deleteRecord(@PathVariable String key) {
    return storage.remove(key).isPresent() ? new Response(Status.SUCCESS, "Successfully removed.")
                                           : new Response(Status.FAILURE, "Record not Found.");
  }

  @GetMapping(value = "dump", produces = MediaType.TEXT_PLAIN_VALUE)
  public FileSystemResource getDump() {
    var stringsMap = storage.getInnerMap().entrySet().stream().collect(Collectors.toMap(
        e -> e.getKey().toString(), e -> e.getValue().toString()));
    Properties properties = new Properties();
    properties.putAll(stringsMap);
    try (OutputStream stream = new FileOutputStream(dumpFileName)) {
      properties.store(stream, LocalDateTime.now().toString());
      stream.flush();
      return new FileSystemResource(new File(dumpFileName));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Data
  @AllArgsConstructor
  @RequiredArgsConstructor
  static class Response {
    private final Status status;
    private final String message;
    private       Object data;

    enum Status {
      SUCCESS, FAILURE
    }
  }

}
