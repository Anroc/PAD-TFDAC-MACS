import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
import scipy.interpolate


def approximate(x, y):
    half = int(len(y) / 2)
    m = (np.average(y[:half]) - np.average(y[half:])) / (np.average(x[:half]) - np.average(x[half:]))
    midx = np.average(x)
    midy = np.average(y)

    return lambda x : x * m + -midx * m + midy

def findIntersection(l1, l2):
    x1, y1, x2, y2 = 0, l1(0), 50, l1(50)
    x3, y3, x4, y4 = 0, l2(0), 50, l2(50)
    px= ( (x1*y2-y1*x2)*(x3-x4)-(x1-x2)*(x3*y4-y3*x4) ) / ( (x1-x2)*(y3-y4)-(y1-y2)*(x3-x4) )
    py= ( (x1*y2-y1*x2)*(y3-y4)-(y1-y2)*(x3*y4-y3*x4) ) / ( (x1-x2)*(y3-y4)-(y1-y2)*(x3-x4) )
    return px, py


def plot_num_filekeys(ax1, model_data):
    ax1 = model_data.plot(
        ax=ax1,
        x='numUsers',
        y=['rsaNumberOfFileKeys', 'abeNumberOfFileKeys'],
        style=['-', '-'],
        color=['green', 'red']
    )
    ax1.set_ylabel("Number of file keys")
    ax1.set_title("Number of file keys")

    add_attribute_xlabel(ax1, model_data)


def read_csv(filename, title = None):
    model_data = pd.read_csv(filename)
    if title is None:
        title = filename

    fig = plt.figure(figsize=(20,5))
    ax1 = fig.add_subplot(1,3,1)
    plot_avg_time(ax1, model_data)

    ax1 = fig.add_subplot(1,3,2)
    plot_ct_size(ax1, model_data)

    ax1 = fig.add_subplot(1,3,3)
    plot_num_filekeys(ax1, model_data)

    plt.legend()
    plt.show()

def plot_ct_size(ax1, model_data):
    ax1 = model_data.plot(
        ax=ax1,
        x='numUsers',
        y=['rsaAvgCTSize', 'abeAvgCTSize'],
        style=['-', '-'],
        color=['green', 'red']
    )
    ax1.set_title("Cipher text sizes")
    ax1.set_ylabel("Size in [bytes]")
    add_attribute_xlabel(ax1, model_data)

def plot_avg_time(ax1, model_data):
    rsaAvgTimeApprox = approximate(model_data['numUsers'], model_data['rsaAvgTime'])
    abeAvgTimeApprox = approximate(model_data['numUsers'], model_data['abeAvgTime'])
    model_data['rsaAvgTimeApprox'] = rsaAvgTimeApprox(model_data['numUsers'])
    model_data['abeAvgTimeApprox'] = abeAvgTimeApprox(model_data['numUsers'])

    x, y = findIntersection(rsaAvgTimeApprox, abeAvgTimeApprox)

    ax1 = model_data.plot(
        ax=ax1,
        x='numUsers',
        y=['rsaAvgTime', 'rsaMedTime', 'abeAvgTime', 'abeMedTime', 'rsaAvgTimeApprox', 'abeAvgTimeApprox'],
        style=['-', '.', '-', '.', '--', '--'],
        color=['green', 'green', 'red', 'red', 'lightgreen', 'pink']
    )
    ax1.axvline(x=x, color='black', linestyle='--', alpha=0.4, label="Approx intersecting point at %d" % int(x))
    ax1.legend(loc=0)
    add_attribute_xlabel(ax1, model_data)
    print("x = " + str(x))
    ax1.set_ylabel('Execution time in [ms]')
    ax1.set_title("Encryption times")

def add_attribute_xlabel(ax1, model_data):
    ax2 = ax1.twiny()
    ax1.legend(loc=0)
    ax1.set_xlabel("Number of users")
    ax2.set_xticks(model_data['numberOfAttributes'])
    ax2.set_xlabel("Number of attributes")
    ax2.tick_params(axis='x', labelrotation=45)


read_csv("or-policies/encrypt_incrementing_10_attribute_increment_1per200User.csv")
