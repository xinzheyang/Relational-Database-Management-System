def main(f1, f2):
    f1_buffer = [];
    f2_buffer = [];
    line1 = "";
    line2 = "";
    with open(f1, 'r') as f1r:
        line1 = f1r.readline()
        f1_buffer.append(line1)
        while line1:
            line1 = f1r.readline()
            f1_buffer.append(line1)

    with open(f2, 'r') as f2r:
        line2 = f2r.readline()
        f2_buffer.append(line2)
        while line2:
            line2 = f2r.readline()
            f2_buffer.append(line2)

    for l in f2_buffer:
        if not l in f1_buffer:
            print l

if __name__ == '__main__':
    from sys import argv;
    main(argv[1], argv[2])
