package us.wistate.enterprise.aht.logsviewer;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.ServletContext;

import us.wistate.enterprise.aht.Tail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Collections;
import java.util.Base64;

@Controller
public class IndexContoller {
	@Value("${spring.logsDirectoryPath}")
	private String logsDirectoryPath;
	
	@Autowired
	private ServletContext context;

	@GetMapping("/")
	public String getIndex(@RequestParam(required = false) String file,@RequestParam(required = false) String folder,@RequestParam(required = false) String invertBlock, Model model) {
		StringBuilder indexHTML = new StringBuilder("");
		if((folder =="" || folder == null) && (file =="" || file == null)) {
			File directoryPath= new File(logsDirectoryPath);
			if(directoryPath.exists() && directoryPath.isDirectory()) {
				String contents[] = directoryPath.list();
				indexHTML.append("<br>Available Logs:<br>");
				indexHTML.append("<ul>");
				for(int i=0; i<contents.length; i++) {
					if(contents[i].charAt(0) != '.'){
						String encodedFolder = Base64.getEncoder().encodeToString((contents[i]).getBytes());
						indexHTML.append("<li class=\"folder\"><i class=\"bi bi-folder-fill\"></i><a href=\""+context.getContextPath()+"/?folder="+encodedFolder+"\"> " + contents[i] + "</a></li>");
					}
				}
				indexHTML.append("</ul>");
			}else{
				indexHTML.append("The path defined in the properties file does not exist or the current user can not access it or the file is not a directory.");
			}
		}else {
			if((file =="" || file == null)&&(folder != "") ) {
				folder = new String(Base64.getDecoder().decode(folder), StandardCharsets.UTF_8);
				File directoryPath= new File(logsDirectoryPath);
				String contents[] = directoryPath.list();
				if(Arrays.asList(contents).contains(folder)) {
					File directoryPathSelectedFolder= new File(logsDirectoryPath+"/"+folder+"/logs");
					String contentsSelectedFolder[] = directoryPathSelectedFolder.list();
					indexHTML.append("<br><a href=\""+context.getContextPath()+"/\"><i class=\"bi bi-arrow-left-circle-fill\"></i></a> Available Logs for <b>"+folder+"</b>:<br>");
					indexHTML.append("<ul>");
					for(int a=0; a<contentsSelectedFolder.length; a++) {
						if(contentsSelectedFolder[a].contains(".log") && !contentsSelectedFolder[a].contains("trace")) {
							String encodedFile = Base64.getEncoder().encodeToString((folder+"/logs/"+contentsSelectedFolder[a]).getBytes());
							indexHTML.append("<li class=\"file\"><i class=\"bi bi-file-earmark-fill\"></i><a href=\""+context.getContextPath()+"/?file="+encodedFile+"\"> " + contentsSelectedFolder[a] + "</a></li>");
						}
				    }
					indexHTML.append("</ul>");
				}else {
					indexHTML.append("<br><a href=\""+context.getContextPath()+"/\"><i class=\"bi bi-arrow-left-circle-fill\"></i></a> <b>This folder does not exist.</b>");
				}
			}
			if((folder =="" || folder == null)&&(file != "") ) {
				file = new String(Base64.getDecoder().decode(file), StandardCharsets.UTF_8);
				File f = new File(logsDirectoryPath+"/"+file);
				if(f.exists() && !f.isDirectory()) { 
					try {
						String encodedFolder = Base64.getEncoder().encodeToString((file.split("/")[0]).getBytes());
						String encodedFile = Base64.getEncoder().encodeToString((file).getBytes());
						indexHTML.append("<br><a href=\""+context.getContextPath()+"/?folder="+encodedFolder+"\"><i class=\"bi bi-arrow-left-circle-fill\"></i> More Logs</a><br><br>");
						if(invertBlock==null){
							indexHTML.append("File: <b>"+file+"</b> (last 100 lines) <a href=\""+context.getContextPath()+"/?file="+encodedFile+"&invertBlock=reverse\"><i class=\"bi bi-arrow-down-up\"></i></a><br><br>");
						}else{
							indexHTML.append("File: <b>"+file+"</b> (last 100 lines) <a href=\""+context.getContextPath()+"/?file="+encodedFile+"\"><i class=\"bi bi-arrow-down-up\"></i></a><br><br>");
						}
						Tail tailFile = new Tail();
						
						List<String> last100lines = tailFile.readLines(logsDirectoryPath+"/"+file,100);
						
						if(invertBlock!=null){
							Collections.reverse(last100lines);
						}
						
						indexHTML.append("<pre><code id=\"fileContent\" class=\"auto\">");
						for (String strLine : last100lines) {
							indexHTML.append(strLine+"\n");
						}
						indexHTML.append("</code></pre>");
						indexHTML.append("<br><a href=\""+context.getContextPath()+"/?folder="+encodedFolder+"\"><i class=\"bi bi-arrow-left-circle-fill\"></i> More Logs</a><br><br>");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}else {
					indexHTML.append("<br><a href=\""+context.getContextPath()+"/\"><i class=\"bi bi-arrow-left-circle-fill\"></i></a> <b>This file does not exist.</b>");
				}
				
				
			}
		}
		
		model.addAttribute("currentFolders",indexHTML.toString());
		return "index";
	}
}
