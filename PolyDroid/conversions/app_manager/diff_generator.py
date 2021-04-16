import os
import sys


def main(argv):
    if len(argv) % 2 != 1 or len(argv) < 5:
        raise Exception('Program format should be '
                        '\'python3 main.py app_name version_no1 version_no2 filepath1 filepath2 filepath1 etc\'')

    app_name, orig_version, new_version = argv[0], argv[1], argv[2]
    diff_tuples = [(argv[i], argv[i + 1]) for i in range(3, len(argv), 2)]

    print(diff_tuples)

    temp_files = []
    for i in range(len(diff_tuples)):
        orig_fp, new_fp = diff_tuples[i]
        temp_file = 'temp_{}.patch'.format(i)
        diff_command_file = 'diff -u {} {} > {}'.format(orig_fp, new_fp, temp_file)
        os.system(diff_command_file)
        temp_files.append(temp_file)

    # make a folder for the app's patches if one doesn't exist
    if app_name not in os.listdir('../patches'):
        os.mkdir(app_name)

    out_file = '../patches/{}/patch{}{}.patch'.format(app_name, orig_version, new_version)
    with open(out_file, 'w') as out_patch_file:
        for temp_file in temp_files:
            with open(temp_file) as temp_patch_file:
                for line in temp_patch_file:
                    if '--- ' in line[:4]:
                        segment_to_keep = line[line.find(app_name):]
                        segment_to_keep = segment_to_keep[segment_to_keep.find('/'):]
                        new_line = '--- $LOCAL_PATH$/apps/' + app_name + '/' + segment_to_keep
                        out_patch_file.write(new_line)
                    elif '+++ ' == line[:4]:
                        segment_to_keep = line[line.find(app_name):]
                        segment_to_keep = segment_to_keep[segment_to_keep.find('/'):]
                        new_line = '+++ $LOCAL_PATH$/apps/' + app_name + '/' + segment_to_keep
                        out_patch_file.write(new_line)
                    else:
                        out_patch_file.write(line)
            out_patch_file.write('\n')

    destroy_temp_pathes()


def destroy_temp_pathes():
    for filename in os.listdir():
        if 'temp' in filename and 'patch' in filename:
            os.remove(filename)


if __name__ == '__main__':
    main(sys.argv[1:])
