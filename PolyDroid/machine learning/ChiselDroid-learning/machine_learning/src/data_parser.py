from data_parser_utilities import get_image_sizes, get_image_resolutions, add_features


class DataParser:

    def __init__(self, input_X_file, input_y_file):

        # create necessary dictionary and list instances
        self._originals, self._debloats, self._scores = [], [], []
        self._features, self._labels = {}, {}
        self._current_threshold = -1

        # get the feature data from the feature file
        with open(input_X_file, encoding='latin-1') as file:
            for row in file:
                debloat = row.split(',')[2]
                if debloat != 'debloating feature':
                    self._originals.append(row) if debloat == 'original' else self._debloats.append(row)

        # get the labels from the label file
        with open(input_y_file, encoding='latin-1') as file:
            for row in file:
                version = row.split(',')[1]
                if version != '' and version[0] == 'v' and version[1] != '0':
                    self._scores.append(row)

        # Create a shallow and deep feature dictionary
        self.feature_tuples = [('icon', 3, None),
                               ('picture', 4, None),
                               ('transition_gif', 5, None),
                               ('textview', 6, None),
                               ('button', 7, None),
                               ('image_sizes', 11, get_image_sizes),
                               ('image_resolutions', 12, get_image_resolutions)]

    def get_features(self, feature_types):
        features_to_parse = [feature for feature in feature_types if feature not in self._features]
        if features_to_parse:
            self._parse_features(features_to_parse)
        return {feature_type: data for feature_type, data in self._features.items() if feature_type in feature_types}

    def get_labels(self, threshold):
        if threshold > 30 or threshold < 0:
            raise Exception('Threshold values must be between 0 and 30')
        if self._current_threshold != threshold:
            self._parse_labels(threshold)
        return self._labels

    # feature_type can be binary_diff, net_orig, net diff, normalized_diff
    def _parse_features(self, feature_types):

        # make sure only legal feature types included
        possible_feature_types = ['binary_diff', 'net_orig', 'net_diff', 'normalized_diff']
        for feature in feature_types:
            if feature not in possible_feature_types:
                raise Exception('Feature Type not Supported')

        # iterate through all the original / version combinations
        for i in range(len(self._originals)):
            original = self._originals[i].split(',')
            debloat = self._debloats[i].split(',')

            # Add the app name and version to appropriate set / dict if it's not already there
            app, version = original[0], original[1][:2]
            if app not in self._features:
                self._features[app] = {version: {}}
            else:
                self._features[app][version] = {}

            # add metadata
            self._features[app][version]['category'] = 0 if debloat[10] == 'multimedia' else 1 if debloat[10] == 'social' else 2

            # add basic features
            for feature, location, extractor_function in self.feature_tuples:
                if extractor_function is None:
                    original_value, debloat_value = int(original[location]), int(debloat[location])
                else:
                    original_value, debloat_value = extractor_function(original[location], debloat[location])
                add_features(self._features, feature_types, app, version, feature, original_value, debloat_value)

    def _parse_labels(self, threshold=24):

        # fill the labels dictionary
        for label in self._scores:
            score_info = label.split(',')
            app = score_info[0]
            version = score_info[1]

            if app not in self._labels:
                self._labels[app] = {version: 0}
            else:
                self._labels[app][version] = 0

            version_score = sum([int(score_info[3]), int(score_info[4]), int(score_info[5])])
            self._labels[app][version] = 1 if version_score > threshold else 0

        self._current_threshold = threshold