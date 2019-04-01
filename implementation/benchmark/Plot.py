import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
from matplotlib.pyplot import cm


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

def read_csv(filename, title=None, x_label="Number of users", subplots = 3):
    if type(filename) is list:
        fig = plt.figure(figsize=(12, 7))
        read_csv_list(filename, fig, x_label)
        png_name = filename[0][:-3] + "png"
    else:
        fig = plt.figure(figsize=(7 * subplots, 5))
        read_csv_single(filename, fig, x_label, subplots)
        png_name = filename[:-3] + "png"

    plt.legend()
    if title is not None:
        fig.suptitle(title, size=16)
    plt.savefig(png_name, bbox_inches='tight')
    plt.show()


def read_csv_single(filename, fig, x_label, subplots):
    model_data = pd.read_csv(filename)

    assert subplots >= 1
    ax1 = fig.add_subplot(1, subplots, 1)
    plot_avg_time(ax1, model_data, x_label)

    if subplots >= 2:
        ax2 = fig.add_subplot(1, subplots, 2)
        plot_ct_size(ax2, model_data, x_label)

    if subplots >= 3:
        ax3 = fig.add_subplot(1, subplots, 3)
        plot_num_filekeys(ax3, model_data, x_label)


def read_csv_list(filenames, fig, x_label):
    model_datas = [pd.read_csv(filename) for filename in filenames]
    num_attr = [filename.split('_')[2][0] for filename in filenames]

    ax1 = fig.add_subplot(1, 1, 1)
    multi_plot_avg_time(ax1, model_datas, x_label, num_attr)


def plot_num_filekeys(ax1, model_data, x_label):
    ax1 = model_data.plot(
        ax=ax1,
        x='x',
        y=['rsaNumberOfFileKeys', 'abeNumberOfFileKeys'],
        style=['-', '-'],
        color=['green', 'red']
    )
    ax1.set_ylabel("Number of file keys")
    ax1.set_title("Number of file keys")
    ax1.set_xlabel(x_label)
    #add_attribute_xlabel(ax1, model_data)


def plot_ct_size(ax, model_data, x_label):
    model_data['rsaAvgCTSize'] /= 1024.
    model_data['abeAvgCTSize'] /= 1024.
    ax = model_data.plot(
        ax=ax,
        x='x',
        y=['rsaAvgCTSize', 'abeAvgCTSize'],
        style=['-', '-'],
        color=['green', 'red']
    )

    ax.set_title("Cipher text sizes")
    ax.set_ylabel("Size in [KB]")
    ax.set_xlabel(x_label)
    #ax = add_attribute_xlabel(ax, model_data)


def plot_avg_time(ax1, model_data, x_label, twinx = True):
    rsaAvgTimeApprox = approximate(model_data['x'], model_data['rsaAvgTime'])
    abeAvgTimeApprox = approximate(model_data['x'], model_data['abeAvgTime'])
    model_data['RSA approx. time'] = rsaAvgTimeApprox(model_data['x'])
    model_data['ABE approx. time'] = abeAvgTimeApprox(model_data['x'])

    model_data['RSA avg. time'] = model_data['rsaAvgTime']
    model_data['RSA med. time'] = model_data['rsaMedTime']
    model_data['ABE avg. time'] = model_data['abeAvgTime']
    model_data['ABE med. time'] = model_data['abeMedTime']

    x, y = findIntersection(rsaAvgTimeApprox, abeAvgTimeApprox)

    ax1 = model_data.plot(
        ax=ax1,
        x='x',
        y=['RSA avg. time', 'RSA med. time', 'ABE avg. time', 'ABE med. time', 'RSA approx. time', 'ABE approx. time'],
        style=['-', '.', '-', '.', '--', '--'],
        color=['green', 'green', 'red', 'red', 'lightgreen', 'pink']
    )
    if x > 0:
        ax1.axvline(x=x, color='black', linestyle='--', alpha=0.4, label="Approx. intersecting point at %d" % int(x))
        ax1.legend(loc=0)
    print("x = " + str(x))
    ax1.set_ylabel('Execution time in [ms]')
    ax1.set_title("Encryption times")
    ax1.set_xlabel(x_label)
    if twinx:
        add_attribute_xlabel(ax1, model_data)

def multi_plot_avg_time(ax1, model_datas, x_label, num_attrs):
    y = []
    c = []
    s = []
    color = iter(cm.rainbow(np.linspace(0, 1, len(model_datas) + 1)))
    for i, model_data in enumerate(model_datas):
        num_attr = str(num_attrs[i])
        if i == 0:
            rsaAvgTimeApprox = approximate(model_data['x'], model_data['rsaAvgTime'])
            model_datas[0]['RSA time approx.'] = rsaAvgTimeApprox(model_data['x'])
            model_datas[0]['RSA avg. time'] = model_data['rsaAvgTime']
            model_datas[0]['RSA med. time'] = model_data['rsaMedTime']
            y += ['RSA time approx.', 'RSA avg. time', 'RSA med. time']
            c1 = next(color)
            c += [c1, c1, c1]
            s += ['--', '-', '.']

        abeAvgTimeApprox = approximate(model_data['x'], model_data['abeAvgTime'])
        model_datas[0][num_attr + ' attr.: Time approx.'] = abeAvgTimeApprox(model_data['x'])
        model_datas[0][num_attr + ' attr.: avg. time'] = model_data['abeAvgTime']
        model_datas[0][num_attr + ' attr.: med. time'] = model_data['abeMedTime']

        y += [num_attr + ' attr.: Time approx.',num_attr + ' attr.: avg. time', num_attr + ' attr.: med. time']
        c2 = next(color)
        c += [c2, c2, c2]
        s += ['--', '-', '.']

        #x, y = findIntersection(rsaAvgTimeApprox, abeAvgTimeApprox)

    ax1 = model_datas[0].plot(
        ax=ax1,
        x='x',
        y=y,
        style=s,
        color=c
    )
        #if x > 0:
        #    ax1.axvline(x=x, color='black', linestyle='--', alpha=0.4, label="Approx intersecting point at %d" % int(x))
        #    ax1.legend(loc=0)

    ax1.set_ylabel('Execution time in [ms]')
    ax1.set_title("Encryption times")
    ax1.set_xlabel(x_label)


def add_attribute_xlabel(ax1, model_data):
    ax2 = ax1.twiny()
    ax2.set_xticks(model_data['numberOfAttributes'])
    ax2.set_xlabel("Number of attributes")
    ax2.tick_params(axis='x', labelrotation=45)
    return ax2

def generate_all():
    from os import walk

    dirs = ['server-artifacts/' + d for d in ['and-policies', 'or-policies', 'join', 'leave']]

    for path in dirs:
        for (dirpath, dirnames, filenames) in walk(path):
            filenames = sorted([path + '/' + filename for filename in filenames if filename[-3:] == 'csv'])
            print("Reading: " + str(filenames))
            if 'policies' in path:
                for filename in filenames:
                    read_csv(filename)
            else:
                read_csv(filenames, subplots=1)


#read_csv("server-artifacts/or-policies/encrypt_incrementing_10_attribute_increment_1per200User.csv")
#read_csv("server-artifacts/and-policies/encrypt_incrementing_10_attribute_increment_1per5User.csv")
#read_csv("join/join_attr_1.csv", x_label = "Number of CipherTexts", subplots = 1)
#read_csv("leave/leave_attr_1_users_2.csv", x_label = "Number of CipherTexts", subplots = 1)

generate_all()
