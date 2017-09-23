########################
# This utility converts lowercase variables in filenames to uppercase.
# For some reason, some of the filenames have lowercases in them and
# that interferes with finding files for specified parameter and spatial type
# at inventory.py:
#
# for aPath in filepaths:
#    if type not in aPath:
#        continue
#    if varname not in aPath:
#        continue
#
#    path = aPath
#
# Important: after running this utility, please delete the era.index file and rerun the inventory.
#########################

import fnmatch
import os

rootPath = 'd:/DB/ERA_Interim/'
pattern = '*.nc'

for root, dirs, files in os.walk(rootPath):
    for filename in fnmatch.filter(files, pattern):
        new_filename = str(filename)
        underscore_index = new_filename[15:].index('_')
        new_filename = new_filename[0:13] + new_filename[13:15+underscore_index].upper() + new_filename[15+underscore_index:]
        os.rename(root+'/'+filename, root+'/'+new_filename)
