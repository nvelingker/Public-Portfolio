import numpy as np


def generate_app_characteristics(passed_versions=[], passed_features=[]):
    return {passed_versions[i]: passed_features[i] for i in range(len(passed_versions))}


def generate_version_info_dict(versions_dict, clf):
    mean_variance_dict = {}
    for version, features in versions_dict.items():
        mean, variance = clf.predict(np.array(features).reshape(1, -1), return_std=True)
        mean_variance_dict[version] = mean[0], variance[0]

    return mean_variance_dict


def objetive_function_probability_of_improvement(options):
    mean_max = -100000
    for option, data_tuple in options.items():
        if data_tuple[0] > mean_max:
            mean_max = data_tuple[0]
    top_version, top_reduction = -1, -1
    for option, data_tuple in options.items():
        mean, variace = data_tuple
        current_reduction = (mean - mean_max - .00001) / variace
        if current_reduction > top_reduction:
            top_version, top_reduction = option, current_reduction
    return top_version, current_reduction


def objetive_function_range_max_variance(options, rng):
    options_in_range = {version: data for version, data in options.items() if rng > abs(4 - data[0])}
    if options_in_range:
        max_var = 0
        for version, data in options_in_range.items():
            if data[1] > max_var:
                max_var = data[1]
                top_version = version
    else:
        max_var = 0
        for version, data in options.items():
            if data[1] > max_var:
                max_var = data[1]
                top_version = version
    return top_version


def evaluate_at_point(version_input, labels):
    # print('Version Selected: {}'.format(version))
    # print('What score would you give this version?')
    # return input()
    view, version = version_input.split('-')

    return labels[view][version]