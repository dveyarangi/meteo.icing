import numpy as np
from netCDF4 import Dataset
import matplotlib.pyplot as plt

files_path = 'D:/DB/Era_Interim'
req_year = '2016'
req_month = '12'
req_day = '01'
req_hour = '00'
# levels(hPa): 0=100 1=150 2=200 3=250 4=300 5=350 6=400 7=450 8=500 9=550
#              10=600 11=650 12=700 13=750 14=800 15=850 16=900 17=950 18=1000
current_level = 15

# Create a histogram for the low resolution data
# Read the file
directory_pre = files_path + '/' + req_year + '/' + req_year + '-' + req_month + '/' + req_day + '/' \
                + req_year + '-' + req_month + '-' + req_day + '_' + req_hour + 'Z_'
directory_post = '_isobaric.nc'

LWC_low_res_nc = Dataset(directory_pre + 'CLWC' + directory_post)
LWC_low_res_data = LWC_low_res_nc.variables['clwc'][:]
LWC_low_res_level_data = np.squeeze(LWC_low_res_data[0, current_level, :, :])
LWC_low_res_1D_array = np.sort(LWC_low_res_level_data.flatten())
print(LWC_low_res_1D_array.max())
print(LWC_low_res_1D_array.mean())
print(LWC_low_res_1D_array.std())

fig = plt.figure()
ax = fig.add_subplot(121)

numBins = 50
ax.hist(LWC_low_res_1D_array[-30:-1], numBins, color='green', alpha=0.8)

lats_low = LWC_low_res_nc.variables['latitude'][:]
lons_low = LWC_low_res_nc.variables['longitude'][:]
latbounds = [ lats_low[0], lats_low[-1]]
lonbounds = [ lons_low[0], lons_low[-1]]

# Create a histogram for the high resolution data
# Read the file
LWC_high_res_nc = Dataset('C:\Users\hatzv\Documents\Geography\Research_help\Icing\meteo.icing\python\ERA-Interim_specific_LWC_Dec-2016.nc')
lats_high = LWC_high_res_nc.variables['latitude'][:]
lons_high = LWC_high_res_nc.variables['longitude'][:]

# latitude lower and upper index
latli = np.argmin(np.abs(lats_high - latbounds[0]))
latui = np.argmin(np.abs(lats_high - latbounds[1]))

# longitude lower and upper index
lonli = np.argmin(np.abs(lons_high - lonbounds[0]))
lonui = np.argmin(np.abs(lons_high - lonbounds[1]))

LWC_high_res_level_data = LWC_high_res_nc.variables['clwc'][0, current_level, latli:latui , lonli:lonui]
LWC_high_res_1D_array = np.sort(LWC_high_res_level_data.flatten())

print(LWC_high_res_1D_array.max())
print(LWC_high_res_1D_array.mean())
print(LWC_high_res_1D_array.std())

#fig = plt.figure()
bx = fig.add_subplot(122)

numBins = 50
bx.hist(LWC_high_res_1D_array[-1000:-1], numBins, color='green', alpha=0.8)
plt.show()
