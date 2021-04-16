from sklearn.linear_model import BayesianRidge, Ridge, LinearRegression, ARDRegression
from data_parser2 import DataParser2
from static_resources import features, correct_versions
from bayes_ridge_utilities import *
import random
import logging
import re

logging.basicConfig(level=logging.INFO)
random.seed(2)


def bayes_active_learning(iters=2, valid_test_apps=[], cpu_coef=0, memory_coef=0, network_coef=0):

    dp = DataParser2(input_X_file='../data/rdc2.csv', input_y_file='../data/results_full2.csv', amplify=False)
    X_dict = dp.get_features(feature_types=[])
    labels = dp.get_labels(threshold=4)
    resource_dict = dp.get_resources()

    all_views = [view for view, data in X_dict.items()]
    pattern = '(A\d*)'
    all_apps = []
    for view in all_views:
        match = re.search(pattern, view)
        app = match.group(0)
        if app not in all_apps:
            all_apps.append(app)
    while True:
        random.shuffle(all_apps)
        test_app = all_apps[:1][0]
        if test_app in valid_test_apps:
            break

    print('Let\'s look at app {}'.format(test_app))

    train_views_list = [view for view in all_views if view[:test_app.find('V')] != test_app]
    test_views_list = [view for view in all_views if view not in train_views_list]

    train_views = {view: version for view, version in X_dict.items() if view in train_views_list}
    test_views = {view: version for view, version in X_dict.items() if view in test_views_list}

    original_test_version_count = 0
    removed_versions = 0
    for view, version_details in test_views.items():
        for version, details in version_details.items():
            original_test_version_count += 1

    ### INCLUDE THIS IF WE SWITCH BACK TO HARD RESOURCE SAVINGS CONSTRAINTS 
            # if resource_dict[view][version]['cpu'] < cpu_savings \
            #         or resource_dict[view][version]['memory'] < memory_savings \
            #         or resource_dict[view][version]['network'] < network_savings:
            #     test_views.pop[view]
            #     removed_versions += 1
    # print('Constraints eliminated {} of {} options'.format(removed_versions, original_test_version_count))

    X = []
    y = []

    for view, versions in train_views.items():
        for version, version_features in versions.items():
            version_list = [version_features[feature] for feature in features if feature in version_features]
            X.append(version_list)
            y.append(labels[view][version])

    test_versions = []
    for view, versions in test_views.items():
        for version, version_features in versions.items():
            version_list = [version_features[feature] for feature in features if feature in version_features]
            X.append(version_list)
            y.append(labels[view][version])
            test_versions.append('{}-{}'.format(view, version))

    test_app_verion_count = len(test_versions)
    X_train, y_train = X[:-test_app_verion_count], y[:-test_app_verion_count]
    X_test, y_test = X[-test_app_verion_count:], y[-test_app_verion_count:]
    X_test_orig = X_test.copy()
    test_versions_orig = test_versions.copy()


    # clf = ARDRegression()
    clf = BayesianRidge(normalize=True)
    clf.fit(X_train, y_train)

    # logging.info('Coefs: {}'.format(clf.coef_))

    for i in range(iters):
        logging.info("Iteration: {}".format(i))

        potential_versions = generate_app_characteristics(test_versions, X_test)

        viable_versions = generate_version_info_dict(potential_versions, clf)

        version_to_query = objetive_function_range_max_variance(viable_versions, rng=2)
        logging.info('Version Selected: {}'.format(version_to_query))

        index_to_swap = test_versions.index(version_to_query)

        for _ in range(10):
            X_train.append(X_test[index_to_swap])
            y_train.append(evaluate_at_point(version_to_query, labels))

        X_test.pop(index_to_swap)
        test_versions.remove(version_to_query)

        clf.fit(X_train, y_train)

        logging.info('Coefs: {}'.format(clf.coef_))

        if not X_test:
            break

    potential_versions = generate_app_characteristics(test_versions_orig, X_test_orig)
    viable_versions = generate_version_info_dict(potential_versions, clf)
    logging.info('Viable Versions: {}'.format(viable_versions))
    logging.info("Actual labels: {}".format(y_test))

    recommended_versions = []
    possibilities = 0
    for view in test_views_list:
        max_savings = -1
        recommended_version = '{}-VOriginal'.format(view)
        possibilities += 1
        recommended_label = 0
        for version, info in viable_versions.items():
            if version[:version.find('-')] == view:
                possibilities += 1
                cpu_effective_savings = cpu_coef * resource_dict[view][version]['cpu']
                memory_effective_savings = memory_coef * resource_dict[view][version]['memory']
                network_effective_savings = network_coef * resource_dict[view][version]['network']
                effective_savings = cpu_effective_savings + memory_effective_savings + network_effective_savings
                if info[0] - 4 > 0 and effective_savings > max_savings:
                    recommended_version = version
                    max_savings = effective_savings
                    recommended_label = info[0]
        print("WINNER", recommended_version, recommended_label)
        recommended_versions.append(recommended_version)
    logging.info("Actual labels: {}".format(y_test))

    return recommended_versions, possibilities


if __name__ == '__main__':
    correct = 0
    views = 0
    possible_versions = 0
    for _ in range(1):

        # ensure savings coefficients sum to 100
        results, possible_options = bayes_active_learning(iters=2, valid_test_apps=['A1', 'A6', 'A11', 'A14', 'A15'], cpu_coef=.4, memory_coef=.3, network_coef=.3)
        for result in results:
            views += 1
            possible_versions += possible_options
            view, version = result.split('-')
            if correct_versions[view] == version:
                correct += 1
    print(correct/views, possible_versions/views)
