package ru.infotecs.intern.storage;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StorageControllerTest {

  @Autowired
  private TestRestTemplate restTemplate;

  @MockBean
  private StorageService storageService;

  @Test
  public void shouldDownloadDump() throws Exception {
    ClassPathResource resource = new ClassPathResource("test_data.txt", getClass());
    given(storageService.createDumpFile()).willReturn(resource.getFile());

    var response = restTemplate.getForEntity("/store/dump",
                                             String.class);

    assertThat(response.getStatusCodeValue()).isEqualTo(200);
    assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION))
        .isEqualTo("attachment; filename=\"dump.txt\"");
    assertThat(response.getBody()).isEqualTo("""
                                             hello@25=world
                                             foo@333=bar
                                             baz@100=baz
                                             """);
  }

}
