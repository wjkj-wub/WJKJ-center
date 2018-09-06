<%@page import="com.miqtech.master.common.helper.PropertiesHelper"%>
<%@ page language="java" pageEncoding="utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="java.io.*"%>
<%@ page import="javax.servlet.ServletContext"%>
<%@ page import="javax.servlet.http.HttpServletRequest"%>
<% 
    //仅做示例用，请自行修改
	String path = "upload";
	path = PropertiesHelper.getProperty(PropertiesHelper.PK_UPLOAD_PATH) + "/" + "UEditor" + "/";
	System.out.println(path);
	String imgStr ="";
	String realpath = getRealPath(request,path)+"/"+path;
	System.out.println(realpath);
	/* String path = "\\upload";
	path = LoadBase.getRESOURCE_REWRITE_DIR() + "/" + LoadBase.getUEDITOR();
	String imgStr ="";
	String realpath = getRealPath(request,path)+"/"+path; */
	List<File> files = getFiles(realpath,new ArrayList());
	
	String url=request.getScheme()+"://";   
	  url+=request.getHeader("host");   
	
	for(File file :files ){
		imgStr+=file.getPath().replace(realpath,path)+"ue_separate_ue";
		//imgStr+=file.getPath().replace(getRealPath(request,path), url)+"ue_separate_ue";
	}
	if(imgStr!=""){
        imgStr = imgStr.substring(0,imgStr.lastIndexOf("ue_separate_ue")).replace(File.separator, "/").trim();
    }
	out.print(imgStr);
%>
<%!
public List getFiles(String realpath, List files) {
	
	File realFile = new File(realpath.replace(File.separator, "/"));
	if (realFile.isDirectory()) {
		File[] subfiles = realFile.listFiles();
		for(File file :subfiles ){
			if(file.isDirectory()){
				getFiles(file.getAbsolutePath(),files);
			}else{
				if(!getFileType(file.getName()).equals("")) {
					files.add(file);
				}
			}
		}
	}
	return files;
}

public String getRealPath(HttpServletRequest request,String path){
	ServletContext application = request.getSession().getServletContext();
	String str = application.getRealPath(request.getServletPath());
	str = ("/").replace(File.separator, "/");
	return new File(str).getPath();
} 

/* public String getRealPath(HttpServletRequest request,String path){
	ServletContext application = request.getSession().getServletContext();
	String str = application.getRealPath(request.getServletPath());
	str = (LoadBase.getUPOAD_PATH()+"/").replace(File.separator, "/");
	return new File(new File(str).getParent()).getParent()+path;
}  */

public String getFileType(String fileName){
	String[] fileType = {".gif" , ".png" , ".jpg" , ".jpeg" , ".bmp"};
	Iterator<String> type = Arrays.asList(fileType).iterator();
	while(type.hasNext()){
		String t = type.next();
		if(fileName.endsWith(t)){
			return t;
		}
	}
	return "";
}
%>