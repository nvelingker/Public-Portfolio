from sklearn.linear_model import LogisticRegression
from sklearn import cross_validation, preprocessing
from random import shuffle
from math import floor
from sklearn.metrics import f1_score
from data_parser_utilities import generate_x_and_y
import random, re



def tester(features, labels, model=LogisticRegression, split='version'):

    split_options = ['random', 'app']
    if split not in split_options:
        raise Exception('The only split options allowed are \'random\' and \'app\'')

    if split == 'random':

        X, y = generate_x_and_y(features, labels)
        X_train, X_test, y_train, y_test = cross_validation.train_test_split(X, y, test_size=.2)

    elif split == 'app':
        all_views = [view for view, data in features.items()]
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

        train_views = {view: version for view, version in features.items() if view in train_views_list}
        test_views = {view: version for view, version in features.items() if view in test_views_list}

        test_app_length = len(test_app[0])

        train_y_dict = {app: version for app, version in labels.items() if app[:test_app_length] != test_app[0]}
        test_y_dict = {app: version for app, version in labels.items() if app[:test_app_length] == test_app[0]}
        print(labels.keys())
        print(test_app)
        print(test_y_dict)

        X_train, y_train = generate_x_and_y(train_views, train_y_dict)
        X_test, y_test = generate_x_and_y(test_views, test_y_dict)
        print(y_test)

    clf = model()
    clf.fit(X_train, y_train)
    predictions = clf.predict(X_test)
    accuracy = clf.score(X_test, y_test)
    total = 0
    correct = 0
    for i in range(len(predictions)):
        if predictions[i] > 4 and y_test[i] > 4:
            correct += 1
        elif predictions[i] < 4 and y_test[i] < 4:
            correct += 1
        total += 1
    accuracy = correct / total

    coefs = clf.coef_

    # print(predictions)
    # print(y_test)
    # print(X_test)
    # print(coefs)

    f1 = 0

    # f1 = f1_score(y_test, predictions, average='binary')

    return clf, accuracy, f1, coefs
