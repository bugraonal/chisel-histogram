import sys
import matplotlib.pyplot as plt

file = sys.argv[1]
with open(file, "r") as f:
    lines = f.readlines()

val = [int(x) for x in lines]

plt.bar([x for x in range(256)], val)
plt.show()
