'''
Created on Oct 4, 2016

@author: Fima
'''

import os 
import sys
import datetime
import json

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
    archive_path = 'H:/icing/Dropbox/icing/archive/era'
    # archive_path = 'H:/icing/Dropbox/icing/archive'
    #archive_path = 'F:\DB\Era_Interim'


    cache_path = ''
     
    #
    # storage string formats:
    timestamp_format = "%Y-%m-%d_%HZ"
    year_month_format = "%Y-%m"
    day_format = "%d"

    #
    # Initializes the index inventory;
    # 
    def __init__(self):
           
        
        # create list of available times 
        self.times = []
        
        self.cache_path = self.archive_path + '/era.index'
        
        # dummy object to access timestamp parsing method; TODO maybe there is prettier way to do this
        self.dt = datetime.datetime(year=1,month=1,day=1)
        
        # create a map of times to files;
        # maps a 'datetime' key to list of files for this date
        # fill the index
        self.index = self.load_index()
        
        self.init_parameter_map()
         
    #
    # Reads archive index and validates files.
    #
    def load_index(self, use_cache = True):

        index = None        
        # test if archive location is valid:
        if not os.path.isdir( self.archive_path ):
            print "Archive path " + self.archive_path + " not found"
            print "Please fix 'archive_path' in inventory.py"
            sys.exit()
            
        # use cache file if available and allowed:
        if use_cache is True :
            index = self.read_index_cache()
        
        # rebuild index if no cache provided:
        if index is None :
            index = self.build_index() 
            self.write_index_cache( index )
        
        # flatten available times list:
        self.times = sorted(list( index.keys() ) )
        
        return index
    
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
        except (OSError, IOError) as e: # failed to  
            print ("Failed to open ", path, " : ", e)
            
        return None
   
    #
    # Retrieves a random dataset from the archive
    #
    def randomSample(self, type, varname):
        return self.getSample( type, varname, self.times[0])
            
    
    def build_index(self):
                # prepare list for all files in the archive:
        files = [];   
        
        index = {};  
         
        print ("Listing archive files...")
        # collect all files in archive:
        for ( dirpath, dirnames, filenames ) in os.walk( self.archive_path ) :
            # print( dirpath )
            files.extend( filenames )
            
        print ("Total %d in archive." % len(files))
         
        print ("Indexing archive...")
        # go over the list of files and fill the index:
        for( filename ) in files:
             
            # extract the timestamp string from the filename:
            date_str = filename[0:14]
            
            # parse the timestamp string; this is the index key
            try: 
                timestamp = self.dt.strptime( date_str, self.timestamp_format );
            except ValueError:
                continue # skip
            
            # note the file path; TODO: seems like waste of ticks and it is:
            filepath = self.create_filename( timestamp, filename )
            
            # skip empty files; this might happen for invalid download configuration; prevent crushing on empty files:
            if os.stat( filepath ).st_size == 0:
                print ("Empty file ", filepath)
                continue
            
            # test that the file is readable:
            # (open and close netcdf dataset)
            #try:
            #    print( "Validating " + filepath )
            #    dataset = Dataset( filepath ) 
            #    dataset.close()
            #except KeyError as e: # this might happen for partially downloaded file
            #    print ("Failed to open ", filepath, " : ", e)
            #    continue
            #except OSError: # otherly corrupted file:
            #    print ("Failed to open ", filepath, " : ")
            #    continue
            
            
            # get or create time-stamped files list:
            if not timestamp in index :
                stamped_files = []
                # create new index entry:
                index[timestamp] = stamped_files
            else :
                # use existing entry:
                stamped_files = index[timestamp] 
                
            # add file path to index:            
            stamped_files.append( filepath )
             
            #print( timestamp, " >>> ", index[timestamp])
        return index
           
    def read_index_cache(self):
        
        serial_index = None
        if os.path.isfile( self.cache_path ) :
            try:
                file_handle = open( self.cache_path, 'r' )
                
                print "Loading cached archive index..."
                serial_index = json.load(file_handle)
                
                file_handle.close()
            except IOError: # cannot read:
                print ("Failed to to read %s" % self.cache_path)
                return None
        else:
            print "No archive index file found."
            return None
        # dummy object to access timestamp parsing method; TODO maybe there is prettier way to do this
        dt = datetime.datetime(year=1,month=1,day=1)
        index = {}
        
        for timestr in serial_index.keys():
            timestamp = self.dt.strptime( timestr, self.timestamp_format );
            filesstr = serial_index[timestr]
            files = []
            for file in filesstr:
                files.append( self.archive_path + file )
                
            index[timestamp] = files
        
        return index
        
    def write_index_cache(self, index):  
           
        file_handle = open( self.cache_path, 'w' )
        
        serial_index = {}
        for timestamp in index.keys():
            timestr = timestamp.strftime(self.timestamp_format)
            files = index[timestamp]
            filesstr = []
            for file in files:
                filesstr.append( file.replace(self.archive_path, '') )
                
            serial_index[timestr] = filesstr
         
        json.dump( serial_index, file_handle )
        
        file_handle.close()
        
    def init_parameter_map(self):
        Inventory.map = {}
        Inventory.map['T'] = 't'
        Inventory.map['U'] = 'u'
        Inventory.map['V'] = 'v'
        Inventory.map['RH'] = 'r'
        Inventory.map['Q'] = 'q'
        Inventory.map['CLWC'] = 'clwc'

               