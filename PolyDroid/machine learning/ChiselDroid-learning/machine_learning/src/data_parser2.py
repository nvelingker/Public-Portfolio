from data_parser_utilities import *


class DataParser2:

    def __init__(self, input_X_file, input_y_file, amplify=False):

        # create necessary dictionary and list instances
        self._originals, self._debloats, self._scores = [], [], []
        self._features, self._labels, self._resources = {}, {}, {}
        self._current_threshold = -1
        self.amplify = amplify

        # get the feature data from the feature file
        with open(input_X_file, encoding='latin-1') as file:
            for row in file:
                version_id, debloat = row.split(',')[1], row.split(',')[3]
                if len(version_id) >= 6 and len(version_id) <= 8:
                    self._originals.append(row) if debloat == 'original' else self._debloats.append(row)

        if len(self._originals) != len(self._debloats):
            raise Exception('Different Sizes')

        # get the labels from the label file
        with open(input_y_file, encoding='latin-1') as file:
            for row in file:
                if row[0] == 'A':
                    self._scores.append(row[:-1]) if '\n' in row else self._scores.append(row)

        # Create a shallow and deep feature dictionary
        self.basic_feature_tuples = [
                                     ('self_size', 4, None),  # E
                                     # ('self_resolution', 5, get_image_resolutions),  # F
                                     ('self_instances', 6, None),  # G
                                     ('self_is_network', 7, None),  # H
                                     ('other_pictures', 8, None),  # I
                                     ('other_textviews', 9, None),  # J
                                     ('other_buttons', 10, None),  # K
                                     ('social_image', 11, None),  # L
                                     ('multimedia_image', 12, None),  # M
                                     ('information_image', 13, None),  # N
                                     ('social_transition', 14, None),  # O
                                     ('multimedia_transition', 15, None),  # P
                                     ('information_transition', 16, None),  # Q
                                     ('other_sizes', 17, get_image_sizes),  # R
                                     # ('other_resolutions', 18, get_image_resolutions),  # S
                                     # ('other_media_network', 19, None),  # T
                                     # ('other_media_database', 20, None),  # U
                                     ('adjacent_textvew', 21, None),  # V
                                     # ('adjacent_textview_size', 22, None),  # W
                                     # ('ratio_large_textviews', 23, None),  # X
                                     ('animation_removed', 24, None),  # Y
                                     ('animation_removed_transition', 25, None),  # Z
                                     ('animation_removed_static', 26, None),  # AA
                                     ('animation_removed_image_size', 27, None),  # AB
                                     ('animation_run_time', 28, None),  # AC
                                     ('image_reduction_score', 5, get_image_reduction_score),
                                     ('is_removed', 2, check_if_removed)
                                     ]

        self.reduction_score_amplified_features = [
                                                   'self_size_a',
                                                   # 'self_resolution_a',
                                                   'self_instances_a',
                                                   'self_is_network_a',
                                                   'other_pictures_a',
                                                   'other_textviews_a',
                                                   'other_buttons_a',
                                                   'social_a',
                                                   'multimedia_a',
                                                   'information_a',
                                                   'other_sizes_a',
                                                   # 'other_resolutions_a',
                                                   'other_media_network_a',
                                                   'other_media_database_a',
                                                   'adjacent_textvew_a',
                                                   # 'adjacent_textview_size_a',
                                                   # 'ratio_large_textviews_a',
                                                   'is_removed_a'
                                                   ]

    def get_features(self, feature_types):
        self._parse_features(feature_types)
        return self._features

    def get_resources(self):
        self._parse_resource_calculations()
        return self._resources

    def get_labels(self, threshold):
        if threshold > 8 or threshold < 0:
            raise Exception('Threshold values must be between 0 and 30')
        if self._current_threshold != threshold:
            self._parse_labels(threshold)
        return self._labels

    # feature_type can be binary_diff, net_orig, net diff, normalized_diff
    def _parse_features(self, feature_types):

        # iterate through all the original / version combinations
        for i in range(len(self._originals)):
            original = self._originals[i].split(',')
            debloat = self._debloats[i].split(',')

            # Add the app name and version to appropriate set / dict if it's not already there
            version_position = original[1].find('V')
            app = original[1][:version_position]
            app_debloat = debloat[1][:version_position]
            if app != app_debloat:
                raise Exception('error comparing {} and {}'.format(app, app_debloat))
            version = original[1][version_position:version_position + 2]
            if app not in self._features:
                self._features[app] = {version: {}}
            else:
                self._features[app][version] = {}

            # add basic features
            for feature, location, extractor_function in self.basic_feature_tuples:
                if extractor_function is None:
                    original_value, debloat_value = original[location], debloat[location]
                elif extractor_function is get_image_reduction_score:
                    original_value, debloat_value = get_image_reduction_score(self._features, app, version, original, debloat)
                else:
                    original_value, debloat_value = extractor_function(original[location], debloat[location])
                add_features(self._features, feature_types, app, version, feature, original_value, debloat_value)
            if self.amplify:
                for rsaf in self.reduction_score_amplified_features:
                    image_reduction_score = self._features[app][version]['image_reduction_score']
                    self._features[app][version][rsaf] = self._features[app][version][rsaf[:-2]] * image_reduction_score
                    self._features[app][version].pop(rsaf[:-2])

    def _parse_resource_calculations(self):
        for i in range(len(self._originals)):
            original = self._originals[i].split(',')
            debloat = self._debloats[i].split(',')

            # Add the app name and version to appropriate set / dict if it's not already there
            version_position = original[1].find('V')
            app = original[1][:version_position]
            app_debloat = debloat[1][:version_position]
            if app != app_debloat:
                raise Exception('error comparing {} and {}'.format(app, app_debloat))
            version = original[1][version_position:version_position + 2]
            if app not in self._resources:
                self._resources[app] = {version: {}}
            else:
                self._resources[app][version] = {}

            if original[31] != 'X':
                self._resources[app][version]['cpu'] = (float(original[31]) - float(debloat[31])) / float(original[31]) if float(original[31]) != 0 else 0
                self._resources[app][version]['memory'] = (float(original[32]) - float(debloat[32])) / float(original[32]) if float(original[32]) != 0 else 0
                self._resources[app][version]['network'] = (float(original[33]) - float(debloat[33])) / float(original[33]) if float(original[33]) != 0 else 0

    def _parse_labels(self, threshold=4):

        # fill the labels dictionary
        for label in self._scores:
            app_version, score_string = label.split(',')
            score = float(score_string)
            version_position = app_version.find('V')
            app = app_version[:version_position]
            version = app_version[version_position:version_position + 2]

            if app not in self._labels:
                self._labels[app] = {version: 0}
            else:
                self._labels[app][version] = 0
            self._labels[app][version] = score
        self._current_threshold = threshold