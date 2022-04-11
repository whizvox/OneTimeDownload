package me.whizvox.otdl.test.util;

import me.whizvox.otdl.file.FileInfo;
import me.whizvox.otdl.storage.InputFile;
import me.whizvox.otdl.storage.StorageService;
import me.whizvox.otdl.test.FileServiceTests;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;

public class MockFile {

  public static void copyFromResources(StorageService storage, String fileId) throws IOException {
    try (InputStream in = FileServiceTests.class.getClassLoader().getResourceAsStream("test/" + fileId)) {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      byte[] buffer = new byte[1024];
      int read;
      while ((read = in.read(buffer)) != -1) {
        baos.write(buffer, 0, read);
      }
      ByteArrayInputStream in2 = new ByteArrayInputStream(baos.toByteArray());
      storage.store(InputFile.inputStream(in2, baos.size()), fileId);
    }
  }

  public static final String ANONYMOUS = "R1DZ4vpu966g";

  public static FileInfo asAnonymous() {
    FileInfo info = new FileInfo();
    info.setId(ANONYMOUS);
    info.setFileName("test.txt");
    info.setOriginalSize(445);
    info.setStoredSize(448);
    info.setMd5("db89bb5ceab87f9c0fcc2ab36c189c2c");
    info.setSha1("cd36b370758a259b34845084a6cc38473cb95e27");
    info.setUploaded(LocalDateTime.of(2022, 2, 19, 12, 0, 0));
    info.setAuthToken("e87ced92fe7affc86d1fcc394bda654c28103567855a4513df65");
    info.setExpires(info.getUploaded().plusMinutes(30));
    info.setUser(null);
    return info;
  }

  public static final String VERIFIED_MEMBER = "nOj8sn6xZ-ca";

  public static FileInfo asVerifiedMember() {
    FileInfo info = new FileInfo();
    info.setId(VERIFIED_MEMBER);
    info.setFileName("test1.txt");
    info.setOriginalSize(12);
    info.setStoredSize(16);
    info.setMd5("86fb269d190d2c85f6e0468ceca42a20");
    info.setSha1("d3486ae9136e7856bc42212385ea797094475802");
    info.setUploaded(LocalDateTime.of(2022, 2, 19, 13, 0, 0));
    info.setAuthToken("faca94dc79dcdbc415d186a9bce257231509c7ef5f234d8a1453");
    info.setExpires(info.getUploaded().plusMinutes(90));
    info.setUser(MockUser.verifiedMember());
    return info;
  }

  public static final String CONTRIBUTOR = "7jfeMbtSCGxa";

  public static FileInfo asContributor() {
    FileInfo info = new FileInfo();
    info.setId(CONTRIBUTOR);
    info.setFileName("test2.txt");
    info.setOriginalSize(12);
    info.setStoredSize(16);
    info.setMd5("86fb269d190d2c85f6e0468ceca42a20");
    info.setSha1("d3486ae9136e7856bc42212385ea797094475802");
    info.setUploaded(LocalDateTime.of(2022, 2, 19, 14, 0, 0));
    info.setAuthToken("d09277d91504bc377cd85101c506ed9377b96d59e71e304b873e");
    info.setExpires(info.getUploaded().plusMinutes(4000));
    info.setUser(MockUser.contributor());
    return info;
  }

}
