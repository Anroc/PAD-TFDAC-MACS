import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
from matplotlib.pyplot import cm

def read_csv(filename):
    return pd.read_csv(filename)

def process(md):
    hist = dict()
    for i,x in enumerate(md['users']):
        if x not in hist:
            hist[x] = {}
            hist[x]['u'] = x
            hist[x]['d'] = md['devices'][i] / x
        else:
            hist[x]['u'] += 1
            hist[x]['d'] += md['devices'][i] / x

    md = pd.DataFrame(columns=['x', 'u', 'd'])
    md['x'] = hist.keys()
    md['u'] = [x['u'] for x in hist.values()]
    md['d'] = [x['d'] for x in hist.values()]
    print(md.head())
    return md

def plot(model_data):
    fig = plt.figure(figsize=(12,5))
    ax2  = fig.add_subplot(1,1,1)
    max = model_data['devices'].max()

    rng = (model_data['devices'].min(), max)
    model_data['users'].plot.hist(range=rng, bins=max, align='left', rwidth=0.5, label="accumulated users")
    model_data['devices'].plot.hist(range=rng, bins=max, align='mid', rwidth=0.5, label="accumulated devices")
    plt.legend()
    plt.xlim([1,max])
    plt.xticks(np.linspace(2, max, max-1))
    plt.xlabel("Number of share members")
    plt.ylabel("Occurrence")
    ax2.set_xlabel("Number of members")
    ax2.tick_params(axis='x', labelrotation=45)
    plt.title("Group distribution per user and devices in Bdrive")
    plt.savefig("./bdrive/share-distribution.png")
    plt.show()

def plot2(model_data):
    plt.figure(figsize=(20, 200))
    model_data.plot.bar(x = 'x', y=['u', 'd'],label=["accumulated users", "accumulated devices"])
    plt.legend()
    plt.xlabel("Number of share members")
    plt.ylabel("Occurrence")
    plt.title("Group distribution per user and devices in Bdrive")
    plt.savefig("./bdrive/share-distribution.png")
    plt.show()

md = read_csv("./bdrive/Bdrive_share_distribution.csv")
plot2(process(md))
