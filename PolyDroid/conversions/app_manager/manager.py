import os
import sys
import json


def main(argv):
    current_versions = read_json()

    if not argv or (len(argv) > 1 and len(argv) % 1 == 1):
        raise Exception('Type \'main.py versions\' for all app versions '
                        'or enter in args in the form \'main.py app_name version app_name version ...\'')
    if argv[0] == 'versions':
        print(current_versions)
        return

    apps_to_update = {}
    for i in range(0, len(argv), 2):
        apps_to_update[argv[i]] = int(argv[i + 1])

    print(current_versions)
    print(apps_to_update)
    filepath_prefix = os.getcwd()[:-12]

    for app, new_version in apps_to_update.items():
        old_version = current_versions[app]
        if old_version != 0:
            make_local_patch_copy(filepath_prefix, app, old_version, 0)
            patch_command = 'patch -s -p0 < ../patches/{}/patch{}0_copy.patch'.format(app, old_version)
            os.system(patch_command)
        if new_version != 0:
            make_local_patch_copy(filepath_prefix, app, 0, new_version)
            patch_command = 'patch -s -p0 < ../patches/{}/patch0{}_copy.patch'.format(app, new_version)
            os.system(patch_command)
        current_versions[app] = new_version
        destroy_local_patch_copies(filepath_prefix, app, old_version, new_version)

    write_json(current_versions)


def read_json():
    with open('versions.json') as version_json:
        return json.load(version_json)


def make_local_patch_copy(filepath_prefix, app, old_version, new_version):
    temp_filepath = '{}/patches/{}/patch{}{}_copy.patch'.format(filepath_prefix, app, old_version, new_version)
    with open(temp_filepath, 'w') as temp_file:
        with open('{}/patches/{}/patch{}{}.patch'.format(filepath_prefix, app, old_version, new_version)) as old_file:
            for line in old_file:
                if line[4:16] == '$LOCAL_PATH$':
                    new_line = line[0:4] + filepath_prefix + line[16:]
                    temp_file.write(new_line)
                else:
                    temp_file.write(line)


def destroy_local_patch_copies(filepath_prefix, app, old_version, new_version):
    for filename in os.listdir('{}/patches/{}'.format(filepath_prefix, app)):
        if filename == 'patch{}0_copy.patch'.format(old_version) or filename == 'patch0{}_copy.patch'.format(new_version):
            os.remove('{}/patches/{}/{}'.format(filepath_prefix, app, filename))


def write_json(current_versions):
    with open('versions.json', 'w') as version_json:
        json.dump(current_versions, version_json)


if __name__ == '__main__':
    main(sys.argv[1:])
