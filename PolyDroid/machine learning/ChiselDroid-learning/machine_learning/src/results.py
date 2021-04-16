import pandas as pd

full = pd.read_csv('results.csv')
full['average_across_users'] = (full['Jiani '] + full['Brian'] + full['Neelay']) / 3
# print(full)
img_rem = full.loc[full['Transformation Type'] == 'Image Removed']
print("Image Removed Mean :\n", img_rem.mean(axis=0))
img_rem = full.loc[full['Transformation Type'] == 'Image Reduced']
print("Image Reduced Mean :\n", img_rem.mean(axis=0))
# img_rem = full.loc[full['Transformation Type'] == 'Animation/Transition Removed']
# print("Animation/Transition Removed :\n", img_rem.std(axis=0))
# img_rem = full.loc[full['Transformation Type'] == 'Misc.']
# print("Misc. :\n", img_rem.std(axis=0))

img_rem = full.loc[full['Transformation Type'] == 'Image Removed']
print("Image Removed :\n", img_rem.std(axis=0))
img_rem = full.loc[full['Transformation Type'] == 'Image Reduced']
print("Image Reduced :\n", img_rem.std(axis=0))
# img_rem = full.loc[full['Transformation Type'] == 'Animation/Transition Removed']
# print("Animation/Transition Removed :\n", img_rem.std(axis=0))
# img_rem = full.loc[full['Transformation Type'] == 'Misc.']
# print("Misc. :\n", img_rem.std(axis=0))