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
    		"2B-GEOPROF-LIDAR.P_R05",
    		//"2B-CWC-RVOD.P_R04"
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

		new CloudSatDownloader( login, password );
	}

	byte[] buffer;

	FTPClient ftp;

	public CloudSatDownloader(String login, String password)
	{

		this.buffer = new byte[1024];

		ftp = new FTPClient();
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
	    ftp.enterLocalPassiveMode();
	    ftp.pasv();
//	      ftp.setPassiveNatWorkaround( true );
	    String.join("" , PRODUCTS);

	    String lastFile = null;
	    int failCount = 0;
	    for( String productName : PRODUCTS )
	    {
	    	  //ftp.changeWorkingDirectory("..");
	    	  //ftp.changeWorkingDirectory(productName);
	    	  //ftp.pasv();
	    	FTPFile [] yearDirs = ftp.listDirectories( productName );
	    	if( yearDirs.length == 0)
	    	    throw new IllegalStateException("Something is wrong, expected folders not found.");
	    	for( FTPFile yearDir : yearDirs )
	    	{
	    	    int year = Integer.parseInt(yearDir.getName());

	    		String yearDirName =  productName + "/" + yearDir.getName();
	    		FTPFile [] dayDirs = ftp.listDirectories( yearDirName );
		    	if( dayDirs.length == 0)
		    	    throw new IllegalStateException("Something is wrong, expected folders not found.");
		    	for( FTPFile dayDir : dayDirs )
		    	{
		    	    int dayOfYear = Integer.parseInt( dayDir.getName());

	    			DateTime datetime = new DateTime(0).withYear( year ).withDayOfYear(dayOfYear);
		    		String dayDirName = yearDirName +  "/" + dayDir.getName();
		    		FTPFile [] files = ftp.listFiles( dayDirName );
			    	if( files.length == 0)
			    	    throw new IllegalStateException("Something is wrong, expected folders not found.");
		    		for(int fidx = 0; fidx < files.length; )
		    		{
		    			FTPFile file = files[fidx];
		    			String remoteFilename = dayDirName + "/" + file.getName();

		    			String localFilename = DOWNLOAD_DIR + "/" +
		    				  DataStamp.monthYearFormat.print( datetime ) + "/" +
		    				  DataStamp.dayFormat.print( datetime ) + "/" +
		    				  file.getName();

		    			File localFile = new File( localFilename );

		    			Result result = this.getFile( remoteFilename, localFile );
		    			if( result != Result.RESTART)
		    			{
		    			    fidx ++;
		    			    failCount = 0;
		    			    lastFile = null;
		    			}
		    			else
		    			{
		    				if( localFile.exists()) // cleanup
		    					localFile.delete();
		    				
		    				failCount ++;
		    				if( failCount > 3)
		    				{
			    			    fidx ++;
			    			    failCount = 0;
			    			    lastFile = null;
			    			}
		    			}
		    		}
		    	}
	    	}

	    }

	      ftp.logout();
	    }
	    catch(IOException e) { e.printStackTrace(); }
	    finally {
	      if(ftp.isConnected()) {
	        try {
	          ftp.disconnect();
	        }
	        catch(IOException ioe) { ioe.printStackTrace(); }
	      }
	      System.out.println("Exited.");
	    }
	}

	public static enum Result {
		DONE, RESTART, IGNORE
	}

	private Result getFile(String remoteFilename, File localFile)
	{
		String localFilename = localFile.getAbsolutePath();
		
		File localUnzippedFile = new File( localFilename.substring(0, localFilename.length()-4) );
		
		boolean isZip = remoteFilename.endsWith(".zip");
		File fileToTest = isZip ? localUnzippedFile : localFile;
		if( fileToTest.exists() && fileToTest.length() != 0)
			{
				System.out.println("Skipping " + localFile);
				return Result.IGNORE;
			}

		try
		{
			Files.createParentDirs( localFile );
		}
		catch( IOException x ) { throw new RuntimeException( x ); }

		try (FileOutputStream stream = new FileOutputStream( localFile ))
		{

		  System.out.println("Downloading " + localFile);
		  ftp.retrieveFile(remoteFilename, stream);
		}
		catch(IOException e) 
		{
			System.out.println("Failed to download " + localFile);
			e.printStackTrace();
			return Result.RESTART;  
		}
		
		if( isZip )
		{
			File newFile = null;
			try ( ZipInputStream  zipStream = new ZipInputStream (new FileInputStream(localFile)); )
			{
				ZipEntry ze = zipStream.getNextEntry();
	
				while(ze!=null)
				{
	
					String fileName = ze.getName();
			        newFile = new File(localFile.getParent() + File.separator + fileName);
	
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
				  e.printStackTrace();
				  localFile.delete();
				  if( newFile != null )
					  newFile.delete();
	
				  return Result.RESTART;
			}
			localFile.delete();
		}
		return Result.DONE;

	}
}
