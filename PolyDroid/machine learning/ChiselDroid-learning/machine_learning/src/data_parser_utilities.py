import numpy as np
from scipy.stats import norm
from scipy.optimize import minimize


def get_image_sizes(original, debloat):
    image_size_list_orig = original.split(';')
    image_size_list_debloat = debloat.split(';')
    image_size_orig = sum([float(i) for i in image_size_list_orig if i != '']) / len(image_size_list_orig)
    image_size_debloat = sum([float(i) for i in image_size_list_debloat if i != '']) / len(image_size_list_debloat)
    return image_size_orig, image_size_debloat


def get_image_resolutions(original, debloat):
    image_resolution_list_orig = original.split(';')
    resolutions_orig, resolutions_debloat = 0, 0

    for image in image_resolution_list_orig:
        if image != '':
            a, b = image.split('x')
            resolutions_orig += (float(a) * float(b))

    image_resolution_list_debloat = debloat.split(';')
    for image in image_resolution_list_debloat:
        if image != '':
            a, b = image.split('x')
            resolutions_debloat += (float(a) * float(b))

    return resolutions_orig / 100000, resolutions_debloat / 100000


def get_image_reduction_score(features, app, version, original, debloat):
    if float(original[4]) == 0:
        return 0, 0

    a, b = original[29].split('x')
    starting_pixels = float(a) * float(b)
    size = float(original[4])

    debloat_res = 0

    image_resolution_list_debloat = debloat[5].split(';')
    for image in image_resolution_list_debloat:
        if image != '':
            a, b = image.split('x')
            debloat_res += (float(a) * float(b))

    pixels_in_box = starting_pixels * size
    if debloat_res > pixels_in_box:
        reduced = .1
    else:
        reduced = (pixels_in_box - debloat_res) / pixels_in_box
    return reduced ** 2, 0


def check_if_removed(original, debloat):
    return (1, 1) if 'V1' in debloat else (0, 0)


def add_features(features, feature_types, app, version, feature, original_value, debloat_value):
    if not feature_types:
        features[app][version][feature] = float(original_value)

    if 'binary_diff' in feature_types:
        features[app][version]["{}_binary_diff".format(feature)] = 1 if debloat_value - original_value else 0

    if 'net_orig' in feature_types:
        features[app][version]["{}_net_orig".format(feature)] = original_value

    if 'net_diff' in feature_types:
        features[app][version]["{}_net_diff".format(feature)] = debloat_value - original_value

    if 'normalized_diff' in feature_types:
        features[app][version]["{}_normalized_diff".format(feature)] = \
        (debloat_value - original_value) / original_value if original_value != 0 else 0


def generate_x_and_y(features, labels):
    X = []
    y = []

    for app, data in features.items():
        for version, features in data.items():
            x = []
            for feature_name, value in features.items():
                x.append(value)
            X.append(x)
            y.append(labels[app][version])
    # mm_scaler = preprocessing.MinMaxScaler()
    # X = mm_scaler.fit_transform(X)
    # print(X)
    # X = preprocessing.normalize(X)
    return X, y


def expected_improvement(X, X_sample, Y_sample, gpr, xi=0.01):

    '''
    Computes the EI at points X based on existing samples X_sample and Y_sample using a Gaussian process surrogate
    model. Args: X: Points at which EI shall be computed (m x d). X_sample: Sample locations (n x d). Y_sample: Sample
    values (n x 1). gpr: A GaussianProcessRegressor fitted to samples. xi: Exploitation-exploration trade-off parameter.
    Returns: Expected improvements at points X.
    '''

    mu, sigma = gpr.predict(X, return_std=True)
    mu_sample = gpr.predict(X_sample)

    sigma = sigma.reshape(-1, X_sample.shape[1])

    # Needed for noise-based model,
    # otherwise use np.max(Y_sample).
    # See also section 2.4 in [...]
    mu_sample_opt = np.max(mu_sample)

    with np.errstate(divide='warn'):
        imp = mu - mu_sample_opt - xi
        Z = imp / sigma
        ei = imp * norm.cdf(Z) + sigma * norm.pdf(Z)
        ei[sigma == 0.0] = 0.0

    return ei


def propose_location(acquisition, X_sample, Y_sample, gpr, bounds, n_restarts=25):

    '''
    Proposes the next sampling point by optimizing the acquisition function. Args: acquisition: Acquisition
    function. X_sample: Sample locations (n x d). Y_sample: Sample values (n x 1). gpr: A GaussianProcessRegressor
    fitted to samples. Returns: Location of the acquisition function maximum.
    '''

    dim = X_sample.shape[1]
    min_val = 1
    min_x = None

    def min_obj(X):
        # Minimization objective is the negative acquisition function
        return -acquisition(X.reshape(-1, dim), X_sample, Y_sample, gpr)

    # Find the best optimum by starting from n_restart different random points.
    for x0 in np.random.uniform(bounds[:, 0], bounds[:, 1], size=(n_restarts, dim)):
        res = minimize(min_obj, x0=x0, bounds=bounds, method='L-BFGS-B')
        if res.fun < min_val:
            min_val = res.fun[0]
            min_x = res.x

    return min_x.reshape(-1, 1)
