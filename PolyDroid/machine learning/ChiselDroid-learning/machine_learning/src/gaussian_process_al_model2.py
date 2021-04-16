from sklearn.gaussian_process import GaussianProcessRegressor, kernels
from sklearn.gaussian_process.kernels import ConstantKernel, Matern
import numpy as np
from data_parser_utilities import propose_location, expected_improvement
from data_parser2 import DataParser2
from sklearn.linear_model import LinearRegression, BayesianRidge
from sklearn.naive_bayes import GaussianNB
from sklearn.preprocessing import normalize


def kernel(a, b):
    """ GP squared exponential kernel """
    kernelParameter = 0.1
    sqdist = np.sum(a**2,1).reshape(-1,1) + np.sum(b**2,1) - 2*np.dot(a, b.T)
    return np.exp(-.5 * (1/kernelParameter) * sqdist)


def main():

    dp = DataParser2(input_X_file='../data/rdc2.csv', input_y_file='../data/results_full2.csv', amplify=True)
    features = dp.get_features(feature_types=[])
    labels = dp.get_labels(threshold=4)

    feature_names = ['self_size', 'self_resolution', 'self_instances', 'self_is_network',  'other_pictures',
                     'other_textviews', 'other_buttons', 'social', 'multimedia', 'information', 'other_sizes',
                     'other_resolutions', 'other_media_network', 'other_media_database', 'adjacent_textvew',
                     'adjacent_textview_size', 'ratio_large_textviews', 'image_reduction_score', 'self_size_a',
                     'self_resolution_a', 'self_instances_a', 'self_is_network_a',  'other_pictures_a',
                     'other_textviews_a', 'other_buttons_a', 'social_a', 'multimedia_a', 'information_a',
                     'other_sizes_a', 'other_resolutions_a', 'other_media_network_a', 'other_media_database_a',
                     'adjacent_textvew_a', 'adjacent_textview_size_a', 'ratio_large_textviews_a']

    X_init = []
    y_init = []

    for app, versions in features.items():
        for version, version_features in versions.items():
            version_list = []
            for feature_name in feature_names:
                if feature_name in version_features:
                    version_list.append(version_features[feature_name])
            X_init.append(version_list)
            y_init.append(labels[app][version])

    # clf = LinearRegression()
    # clf.fit(X_init, y_init)
    clf = BayesianRidge()
    clf.fit(X_init, y_init)

    coefs = clf.coef_

    print("######")
    print(coefs)


    kernel = kernels.Matern()
    gpr = GaussianProcessRegressor(kernel=kernel)

    # Initialize samples

    X_sample = np.array(normalize(np.array(X_init).T)).T
    # X_sample = np.array((X_init))
    #X_sample = np.array(X_init)
    #X_sample = (X_sample - np.mean(X_sample, axis=1)) / np.std(X_sample, axis=1)
    print(X_sample)
    Y_sample = np.array(y_init)

    # Number of iterations
    n_iter = 10
    bounds = np.array([[0.0, 9.0]])
    gpr.fit(X_sample, Y_sample)

    # for i in range(n_iter):
    #     gpr.fit(X_sample, Y_sample)
    #
    #     # Obtain next sampling point from the acquisition function (expected_improvement)
    #     X_next = propose_location(expected_improvement, X_sample, Y_sample, gpr, bounds)
    #
    #     # Obtain next noisy sample from the objective function
    #     Y_next = f(X_next, noise)
    #
    #     # Add sample to previous samples
    #     X_sample = np.vstack((X_sample, X_next))
    #     Y_sample = np.vstack((Y_sample, Y_next))



if __name__ == '__main__':
    main()