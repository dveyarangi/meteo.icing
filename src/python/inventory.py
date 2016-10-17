'''
Created on Oct 4, 2016

@author: Fima
'''

import os 
import datetime

from netCDF4 import MFDataset
from netCDF4 import Dataset
#
# This class performs listing of the archive files and extracts timeline index.
#
# The archive storage is structure as:
# 
#   > <year>-<month>  
#      > <day>
#         > <year>-<month>-<day>_<hour>Z_<parameter>_<spatial structure>.nc
#
# Consequently, there is a file for a (time, parameter) couple.
#
#
class Inventory:
    
    # Archive path
    archive_path = 'E:/Development/workspaces/meteo/ecmwf-api-client/download/era-interim'
     
    #
    # storage string formats:
    timestamp_format = "%Y-%m-%d_%HZ"
    year_month_format = "%Y-%m"
    day_format = "%d"

    #
    # Initializes the index inventory;
    # 
    def __init__(self):
           
        # create a map of times to files;
        # maps a 'datetime' key to list of files for this date
        self.index = {}
        
        # create list of available times 
        self.times = []
        
        # fill the index
        self.read_index()
 
    #
    # Reads archive index and validates files.
    #
    def read_index(self):
        
        # prepare list for all files in the archive:
        files = [];    
        
        # dummy object to access timestamp parsing method; TODO maybe there is prettier way to do this
        dt = datetime.datetime(year=1,month=1,day=1)
        
        print ("Listing archive files...")
        # collect all files in archive:
        for ( dirpath, dirnames, filenames ) in os.walk( self.archive_path ) :
            # print( dirpath )
            files.extend( filenames )
         
        print ("Indexing archive...")
        # go over the list of files and fill the index:
        for( filename ) in files:
             
            # extract the timestamp string from the filename:
            date_str = filename[0:14]
            
            # parse the timestamp string; this is the index key
            timestamp = dt.strptime( date_str, self.timestamp_format );
            
            # note the file path; TODO: seems like waste of ticks and it is:
            filepath = self.create_filename( timestamp, filename )
            
            # skip empty files; this might happen for invalid download configuration; prevent crushing on empty files:
            if os.stat( filepath ).st_size == 0:
                print ("Empty file ", filepath)
                continue
            
            # test that the file is readable:
            # (open and close netcdf dataset)
            try:
                #print( "Validating " + filepath )
                dataset = Dataset( filepath ) 
                dataset.close()
            except KeyError as e: # this might happen for partially downloaded file
                print ("Failed to open ", filepath, " : ", e)
                continue
            except OSError: # otherly corrupted file:
                print ("Failed to open ", filepath, " : ")
                continue
            
            
            # get or create time-stamped files list:
            if not timestamp in self.index :
                stamped_files = []
                # create new index entry:
                self.index[timestamp] = stamped_files
            else :
                # use existing entry:
                stamped_files = self.index[timestamp] 
                
            # add file path to index:            
            stamped_files.append( filepath )
             
            #print( timestamp, " >>> ", self.index[timestamp])

        # flatten available times list:
        self.times = sorted(list( self.index.keys() ) )
        
        return files
    
    #
    # creates full path to given file
    #
    def create_filename( self, timestamp, filename ):
        return self.archive_path + "/" + timestamp.strftime( self.year_month_format ) +\
                                   "/" + timestamp.strftime( self.day_format ) +\
                                   "/" + filename
     
    #    
    # retrieve dataset for specified data spatial type, variable name and time
    #
    def getSample(self, type, varname, time):

        # list of all data files for the specified time:
        filepaths = self.index[ time ]
        
        # filenames parameter part matches netcdf parameter name in uppercase:
        varname = varname.upper()
        path = ""
        
        # finding file for specified parameter and spatial type:
        for aPath in filepaths:
            if type not in aPath:
                continue
            if varname not in aPath:
                continue
            
            path = aPath
                
        try: 
            # open netcdf dataset:
            ds = Dataset( path ) 
            return ds
        except OSError: # failed to  
            print ("Failed to open " + path + " : ")
            
        return None
   
    #
    # Retrieves a random dataset from the archive
    #
    def randomSample(self, type, varname):
        return self.getSample( type, varname, self.times[0])
            
    
                            