'''
Created on Oct 4, 2016

'''

from netCDF4 import Dataset

from structure import MDataset

# create archive dataset:
dataset = MDataset("isobaric")


print (dataset["t"].shape)


print (dataset.lats.shape)

