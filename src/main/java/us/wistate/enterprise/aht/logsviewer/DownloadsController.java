package us.wistate.enterprise.aht.logsviewer;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.FileCopyUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.io.InputStream;
import java.io.BufferedInputStream;

@RestController
@RequestMapping("/download")
public class DownloadsController {
	@Value("${spring.logsDirectoryPath}")
	private String logsDirectoryPath;
	
	@RequestMapping("/file/")
	public void downloadLog(HttpServletRequest request, HttpServletResponse response,@RequestParam("fileName") String fileName) throws IOException {
		fileName = new String(Base64.getDecoder().decode(fileName), StandardCharsets.UTF_8);
		File file = new File(logsDirectoryPath +"/"+ fileName);
		if (file.exists()) {

			String mimeType = URLConnection.guessContentTypeFromName(file.getName());
			if (mimeType == null) {
				mimeType = "application/octet-stream";
			}
			response.setContentType(mimeType);
			response.setHeader("Content-Disposition", String.format("inline; filename=\"" + file.getName() + "\""));
			response.setContentLength((int) file.length());
			InputStream inputStream = new BufferedInputStream(new FileInputStream(file));
			FileCopyUtils.copy(inputStream, response.getOutputStream());
		}
		
	}
	
	
}