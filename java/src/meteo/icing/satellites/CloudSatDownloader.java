package meteo.icing.satellites;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.common.io.Files;

import meteo.icing.era.DataStamp;

public class CloudSatDownloader
{
    public static String CLOUDSAT_SERVER = "ftp.cloudsat.cira.colostate.edu";

    public static String [] PRODUCTS = new String [] {
    		//"2B-GEOPROF-LIDAR.P2_R04",
    		"2B-CWC-RVOD.P_R04"
    };
	public static DateTimeFormatter dayFormat = DateTimeFormat.forPattern("dd");
	public static DateTimeFormatter monthYearFormat = DateTimeFormat.forPattern("yyyy-MM");

    public static String DOWNLOAD_DIR = "H:/icing/Dropbox/icing/archive/cloudsat";

    public static String LOGIN = "";
    public static String PASSW = "p1t3kantr0p";


	public static void main( String [] args )
	{

		String login = args[0];
		String password = args[1];

		byte[] buffer = new byte[1024];

		FTPClient ftp = new FTPClient();
	    FTPClientConfig config = new FTPClientConfig();

	    //config.set // change required options
	    // for example config.setServerTimeZoneId("Pacific/Pitcairn")
	    ftp.configure( config );
	    boolean error = false;
	    try {
	      int reply;
	      ftp.connect( CLOUDSAT_SERVER );
	      System.out.println("Connected to " + CLOUDSAT_SERVER + ".");
	      System.out.print( ftp.getReplyString() );

	      // After connection attempt, you should check the reply code to verify
	      // success.
	      reply = ftp.getReplyCode();

	      if(!FTPReply.isPositiveCompletion(reply)) {
	        ftp.disconnect();
	        System.err.println("FTP server refused connection.");
	        System.exit(1);
	      }

	      if(!ftp.login(login, password))
	      {
		        ftp.disconnect();
		        System.err.println("Failed to log in: " + ftp.getReplyString() );
		        System.exit(1);
	      }

	      ftp.setFileType(FTP.BINARY_FILE_TYPE);
	     // ftp.enterLocalPassiveMode();
	     // ftp.pasv();
//	      ftp.setPassiveNatWorkaround( true );

	      for( String productName : PRODUCTS )
	      {
	    	  //ftp.changeWorkingDirectory("..");
	    	  //ftp.changeWorkingDirectory(productName);
	    	  //ftp.pasv();
	    	  FTPFile [] yearDirs = ftp.listDirectories( productName );

	    	  for( FTPFile yearDir : yearDirs )
	    	  {
	    		  int year = Integer.parseInt(yearDir.getName());

	    		  String yearDirName =  productName + "/" + yearDir.getName();
	    		  FTPFile [] dayDirs = ftp.listDirectories( yearDirName );
		    	  for( FTPFile dayDir : dayDirs )
		    	  {
		    		  int dayOfYear = Integer.parseInt( dayDir.getName());

	    			  DateTime datetime = new DateTime(0).withYear( year ).withDayOfYear(dayOfYear);
		    		  String dayDirName = yearDirName +  "/" + dayDir.getName();
		    		  FTPFile [] files = ftp.listFiles( dayDirName );
		    		  for(FTPFile file : files)
		    		  {

		    			  String remoteFilename = dayDirName + "/" + file.getName();

		    			  String localFilename = DOWNLOAD_DIR + "/" +
		    					  DataStamp.monthYearFormat.print( datetime ) + "/" +
		    					  DataStamp.dayFormat.print( datetime ) + "/" +
		    					  file.getName();

		    			  File localFile = new File( localFilename );
		    			  File localUnzippedFile = new File( localFilename.substring(0, localFilename.length()-4) );
		    			  if( localUnzippedFile.exists() && localUnzippedFile.length() != 0)
		    			  {
		    				  System.out.println("Skipping " + localFile);
		    				  continue;
		    			  }

		    			  Files.createParentDirs( localFile );

		    			  try (FileOutputStream stream = new FileOutputStream( localFile ))
		    			  {

		    				  System.out.println("Downloading " + localFile);
		    				  ftp.retrieveFile(remoteFilename, stream);

		    			  }
		    			  catch(IOException e)
		    			  {
		    				  throw new RuntimeException(e);
		    			  }

		    			  try
		    				  (ZipInputStream  zipStream = new ZipInputStream (new FileInputStream(localFile));
		    				  )
		    				  {
		    				  	ZipEntry ze = zipStream.getNextEntry();

		    			    	while(ze!=null){

		    			    	   String fileName = ze.getName();
		    			           File newFile = new File(localFile.getParent() + File.separator + fileName);

		    			           System.out.println("Unzipping " + newFile.getAbsoluteFile());

		    			            //create all non exists folders
		    			            //else you will hit FileNotFoundException for compressed folder
		    			            new File(newFile.getParent()).mkdirs();

		    			            FileOutputStream fos = new FileOutputStream(newFile);

		    			            int len;
		    			            while ((len = zipStream.read(buffer)) > 0) {
		    			            	fos.write(buffer, 0, len);
		    			            }

		    			            fos.close();
		    			            ze = zipStream.getNextEntry();
		    			    	}
		    			  }
		    			  catch(Exception e)
		    			  {
		    				  throw new RuntimeException(e);
		    			  }

		    			  localFile.delete();
		    		  }
		    	  }
	    	  }

	      }

	      ftp.logout();
	    } catch(IOException e) {
	      error = true;
	      e.printStackTrace();
	    } finally {
	      if(ftp.isConnected()) {
	        try {
	          ftp.disconnect();
	        } catch(IOException ioe) {
	          // do nothing
	        }
	      }
	      System.exit(error ? 1 : 0);
	    }
	}
}
