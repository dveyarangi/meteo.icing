from netCDF4 import Dataset

from archive.inventory import Inventory

import numpy as np
from numpy.core.defchararray import upper


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
        self.inventory = Inventory()
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
    def __getitem__(self, varname):

        if varname not in self.variables:  # variable not cached
            variable = MVariable(self.inventory, varname, self.spatialType)
            self.variables[varname] = variable
        else:
            variable = self.variables[varname];

        return variable

    def closestLonIndex(self, lon):

        prev_lon = self.lons[0]
        for (idx, curr_lon) in enumerate(self.lons):
            if lon > curr_lon or lon < prev_lon:
                continue

            if lon < (curr_lon - prev_lon) / 2:
                return idx - 1
            else:
                return idx

            prev_lon = curr_lon

    def closestLatIndex(self, lat):

        prev_lat = self.lats[0]
        for (idx, curr_lat) in enumerate(self.lats):
            if lat < curr_lat or lat > prev_lat:
                continue

            if lat < (curr_lat - prev_lat) / 2:
                return idx - 1
            else:
                return idx

            prev_lat = curr_lat


#
# Matches archive files data set for a single parameter
# 						
class MVariable:
    def __init__(self, inventory, varname, spatialType):

        # keeping archive index reference for file names search:
        self.inventory = inventory
        # inventory spatial type:
        self.spatialType = spatialType

        # sampling the archive to get grid properties:
        sampleSet = self.inventory.randomSample(self.spatialType, varname)

        # retrieving netcdf variable:

        self.varname = Inventory.map[varname.upper()]
        sampleVar = sampleSet.variables[self.varname]

        # retain some variable's parameters for convinience:
        self.shape = sampleVar.shape
        self.units = sampleVar.units

    #
    # overrides [] operator;
    # for usage example sea the header comment
    #
    def __getitem__(self, index):

        # retrieve netcdf dataset for this parameter type:
        # FIXED BY TH 27-Jun-2017: When you want to read the file, you need
        # the variable name on the file, not inside, e.g., for relative humidity
        # the file is named _RH_ but the self.varname is 'r' and thus can't be read.
        # ds = self.inventory.getSample( self.spatialType, self.varname, index )
        ds = self.inventory.getSample(self.spatialType,
                                      self.inventory.map.keys()[self.inventory.map.values().index(self.varname)], index)
        if ds is None:  # returnz zero array if no data found
            grid = None
        else:
            grid = ds.variables[self.varname][0]
            ds.close()

        return grid
