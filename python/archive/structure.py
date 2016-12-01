from netCDF4 import Dataset

from archive.inventory import Inventory  

import numpy as np
#
# Represents a spatial structure stored in the archive
# Provides simple syntax to access archive variables;
#
# for example: 
#       dataset = MDataset("isobaric")
#       datetime = dataset.times[0]
#       level = 5
#       t_grid = dataset["t"][datetime][level]
#
# returns temperature grid for specific time and isobaric level;
# 'level' values can be assessed via 'dataset.level' list 
#
class MDataset:

	def __init__(self, spatialType):   
		# indexing archive:
		self.inventory = Inventory( )
		# variable structure cache:
		self.variables = {}
		# archive type, like 'isobaric'; TODO: this should be automatic later on
		self.spatialType = spatialType 
		
		# caching shared properties:
		sampleSet = self.inventory.randomSample("isobaric", "t")
		
		# cache some frequently used variables:
		self.lons = sampleSet.variables['longitude'][:]
		self.lats = sampleSet.variables['latitude'][:]
		self.levels = sampleSet.variables['level']
		self.times = self.inventory.times

	#
	# overrides [] operator;
	# for example: 
	#       dataset = MDataset("isobaric")
	#       t_timeline = dataset["t"]
	#
	# returns temperature timeline variable
	# 
	def __getitem__(self,varname):
		
		if varname not in self.variables: # variable not cached
			variable = MVariable( self.inventory, varname, self.spatialType)
			self.variables[varname] = variable
		else:
			variable = self.variables[varname];
	
		
		return variable
			
#	
# Matches archive files data set for a single parameter
# 						
class MVariable:

	def __init__(self, inventory, varname, spatialType):
		 
		# keeping archive index reference for file names search:   
		self.inventory = inventory
		# variable name: 
		self.varname = varname
		# inventory spatial type:
		self.spatialType = spatialType
		
		# sampling the archive to get grid properties:
		sampleSet = self.inventory.randomSample( self.spatialType, varname )
		
		# retrieving netcdf variable:
		sampleVar = sampleSet.variables[varname]
		
		# retain some variable's parameters for convinience:
		self.shape = sampleVar.shape
		self.units = sampleVar.units

	#
	# overrides [] operator;
	# for usage example sea the header comment
	#
	def __getitem__(self,index):
		
		# retrieve netcdf dataset for this parameter type:
		ds = self.inventory.getSample( self.spatialType, self.varname, index )
		if ds is None: # returnz zero array if no data found
			grid = None
		else:
			grid = ds.variables[self.varname][0]
			ds.close()
		
		return grid
