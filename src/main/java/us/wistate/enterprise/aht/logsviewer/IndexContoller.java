package us.wistate.enterprise.aht.logsviewer;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
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
import java.util.Arrays;

@Controller
public class IndexContoller {
	@Value("${spring.logsDirectoryPath}")
	private String logsDirectoryPath;
	
	@GetMapping("/")
	public String getIndex(@RequestParam(required = false) String file,@RequestParam(required = false) String folder, Model model) {
		StringBuilder indexHTML = new StringBuilder("");
		if((folder =="" || folder == null) && (file =="" || file == null)) {
			File directoryPath= new File(logsDirectoryPath);
			String contents[] = directoryPath.list();
			indexHTML.append("<br>Available Logs:<br>");
			indexHTML.append("<ul>");
			for(int i=0; i<contents.length; i++) {
				if(contents[i].charAt(0) != '.'){
					indexHTML.append("<li class=\"folder\"><i class=\"bi bi-folder-fill\"></i><a href=\"/?folder="+contents[i]+"\"> " + contents[i] + "</a></li>");
				}
			}
			indexHTML.append("</ul>");
		}else {
			if((file =="" || file == null)&&(folder != "") ) {
				File directoryPath= new File(logsDirectoryPath);
				String contents[] = directoryPath.list();
				if(Arrays.asList(contents).contains(folder)) {
					File directoryPathSelectedFolder= new File(logsDirectoryPath+"/"+folder+"/logs");
					String contentsSelectedFolder[] = directoryPathSelectedFolder.list();
					indexHTML.append("<br><a href=\"/\"><i class=\"bi bi-arrow-left-circle-fill\"></i></a> Available Logs for <b>"+folder+"</b>:<br>");
					indexHTML.append("<ul>");
					for(int a=0; a<contentsSelectedFolder.length; a++) {
						if(contentsSelectedFolder[a].contains(".log")) {
							indexHTML.append("<li class=\"file\"><i class=\"bi bi-file-earmark-fill\"></i><a href=\"/?file="+folder+"/logs/"+contentsSelectedFolder[a]+"\"> " + contentsSelectedFolder[a] + "</a></li>");
						}
				    }
					indexHTML.append("</ul>");
				}else {
					indexHTML.append("<br><a href=\"/\"><i class=\"bi bi-arrow-left-circle-fill\"></i></a> <b>This folder does not exist.</b>");
				}
			}
			if((folder =="" || folder == null)&&(file != "") ) {
				File f = new File(logsDirectoryPath+"/"+file);
				if(f.exists() && !f.isDirectory()) { 
					try {
						indexHTML.append("<br><a href=\"/?folder="+file.split("/")[0]+"\"><i class=\"bi bi-arrow-left-circle-fill\"></i> More Logs</a><br><br>");
						indexHTML.append("File: <b>"+file.split("/")[2]+"</b><br>");
						FileInputStream fstream = new FileInputStream(logsDirectoryPath+"/"+file);
						DataInputStream in = new DataInputStream(fstream);
						BufferedReader br = new BufferedReader(new InputStreamReader(in));
						String strLine;
						try {
							indexHTML.append("<pre><code class=\"auto\">");
							while ((strLine = br.readLine()) != null)   {
								indexHTML.append(strLine+"\n");
							}
							indexHTML.append("</code></pre>");
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						try {
							in.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}else {
					indexHTML.append("<br><a href=\"/\"><i class=\"bi bi-arrow-left-circle-fill\"></i></a> <b>This file does not exist.</b>");
				}
				
				
			}
		}
		
		model.addAttribute("currentFolders",indexHTML.toString());
		return "index";
	}
}
