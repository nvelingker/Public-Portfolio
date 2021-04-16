from sklearn.linear_model import BayesianRidge, Ridge, LinearRegression
from sklearn.preprocessing import normalize
from data_parser2 import DataParser2
from static_resources import features
import random
import logging
import re

logging.basicConfig(level=logging.INFO)
# random.seed(0)
import warnings
warnings.filterwarnings(action="ignore", module="scipy", message="^internal gelsd")

def main(iters=2):

    dp = DataParser2(input_X_file='../data/rdc2.csv', input_y_file='../data/results_full2.csv', amplify=True)
    X_dict = dp.get_features(feature_types=[])
    labels = dp.get_labels(threshold=4)

    total = 0
    correct = 0

    for i in range(iters):
        all_views = [view for view, data in X_dict.items()]
        pattern = '(A\d*)'
        all_apps = []
        for view in all_views:
            match = re.search(pattern, view)
            app = match.group(0)
            if app not in all_apps:
                all_apps.append(app)
        random.shuffle(all_apps)
        test_app = all_apps[:1]

        train_views_list = [view for view in all_views if view[:test_app[0].find('V')] != test_app[0]]
        test_views_list = [view for view in all_views if view not in train_views_list]

        train_views = {view: version for view, version in X_dict.items() if view in train_views_list}
        test_views = {view: version for view, version in X_dict.items() if view in test_views_list}

        X_init = []
        y_init = []

        for view, versions in train_views.items():
            for version, version_features in versions.items():
                version_list = [version_features[feature] for feature in features if feature in version_features]
                X_init.append(version_list)
                y_init.append(labels[view][version])

        test_app_versions = []
        test_app_views = []
        for view, versions in test_views.items():
            for version, version_features in versions.items():
                version_list = [version_features[feature] for feature in features if feature in version_features]
                X_init.append(version_list)
                y_init.append(labels[view][version])
            test_app_versions_temp = ['{}-{}'.format(view, version) for version, version_features in versions.items()]
            test_app_versions = test_app_versions + test_app_versions_temp
            test_app_views.append(view)

        test_app_verion_count = len(test_app_versions)
        X_init = normalize(X_init, axis=0)
        X_init_train, y_init_train = X_init[:-test_app_verion_count], y_init[:-test_app_verion_count]
        X_init_test, y_init_test = X_init[-test_app_verion_count:], y_init[-test_app_verion_count:]

        clf = LinearRegression()
        clf.fit(X_init_train, y_init_train)
        predictions = clf.predict(X_init_test)
        # print(test_app_views)
        # print(y_init_test)
        # print(predictions)

        for i in range(len(predictions)):
            if predictions[i] >= 4 and y_init_test[i] >= 4:
                correct += 1
            elif predictions[i] <= 4 and y_init_test[i] <= 4:
                correct += 1
            total += 1
        accuracy = correct / total

    return accuracy


if __name__ == '__main__':
    print(main(iters=10000))

