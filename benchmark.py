import argparse
import numpy as np
import random
import struct
import os

DI_TABLE_SIZE = [3000, 2000, 2000, 7 * 365]
DI_TABLE_NAME = ['CUSTOMER', 'PART', 'SUPPLIER', 'DATE']
DI_SHORTCUT_NAME = ['C', 'P', 'S', 'D']
DI_SCHEMA_NAME = [['CKEY', 'CV'], ['PKEY', 'PV'], ['SKEY', 'SV'], ['DKEY','DV']]
FACT_TABLE_SIZE = 60000
FACT_TABLE_NAME = 'LINEORDER'
FACT_SHORTCUT_NAME = 'L'
FACT_SCHEMA_NAME = ['LKEY', 'CKEY', 'PKEY', 'SKEY', 'DKEY']

def geneate_schema(fileName = 'schema.txt'):
    directory = 'input/db/'
    f = open(directory + fileName, "w+")
    for i in range(len(DI_TABLE_NAME)):
        line = DI_TABLE_NAME[i]
        for name in DI_SCHEMA_NAME[i]:
            line += " " + name
        f.write(line + '\n')
    line = FACT_TABLE_NAME
    for name in FACT_SCHEMA_NAME:
        line += " " + name
    f.write(line)

def generate_queries1(fileName = 'queries.sql', min = 0, max = 50, num_di_tables = 4, num_queries = 20):
    assert(num_di_tables > 0)
    directory = 'input/'
    comp_symbols = ['=', '!=', '>=', "<=", '>', "<"]
    f = open(directory + fileName, 'w+')
    for i in range(num_queries):
        template = "SELECT {} FROM {} WHERE {}"
        select_clause = ''
        from_clause = FACT_TABLE_NAME + " " + FACT_SHORTCUT_NAME
        where_clause = ''

        # from
        index_table = [1] * num_di_tables + [0] * (len(DI_TABLE_NAME) - num_di_tables)
        random.shuffle(index_table)
        for i in range(len(index_table)):
            if index_table[i] == 1:
                from_clause += ', ' + DI_TABLE_NAME[i] + " " + DI_SHORTCUT_NAME[i]

        # where
        where_statment = []
        if num_di_tables > 0:
            for i in range(len(index_table)):
                if index_table[i] == 1:
                    where_statment.append("{}.{}={}.{}".format(FACT_SHORTCUT_NAME, FACT_SCHEMA_NAME[i+1],
                        DI_SHORTCUT_NAME[i], DI_SCHEMA_NAME[i][0]))
        if num_di_tables > 1:
            reach_array = [0] * len(DI_SHORTCUT_NAME)
            max_clauses = num_di_tables - 1
            while True:
                i = random.randint(0, len(index_table) - 1)
                value = random.randint(min, max)
                if np.sum(reach_array) == num_di_tables:
                    break
                if reach_array[i] == 1:
                    continue
                reach_array[i] = 1
                comp = random.choice(comp_symbols)
                where_statment.append("{}.{}{}{}".format(DI_SHORTCUT_NAME[i], DI_SCHEMA_NAME[i][1], comp,
                    value))
        where_clause = where_statment[0]
        for i in range(1, len(where_statment)):
            where_clause += ' AND ' + where_statment[i]

        # select
        select_clause = '*'
        f.write(template.format(select_clause, from_clause, where_clause)+'\n')

def generate_queries2(fileName = 'queries.sql', num_di_tables = 4, num_queries = 20):
    assert(num_di_tables > 0)
    directory = 'input/'
    comp_symbols = ['=', '!=', '>=', "<=", '>', "<"]
    f = open(directory + fileName, 'w+')
    for i in range(num_queries):
        template = "SELECT {} FROM {} WHERE {}"
        select_clause = ''
        from_clause = FACT_TABLE_NAME + " " + FACT_SHORTCUT_NAME
        where_clause = ''

        # from
        index_table = [1] * num_di_tables + [0] * (len(DI_TABLE_NAME) - num_di_tables)
        random.shuffle(index_table)
        for i in range(len(index_table)):
            if index_table[i] == 1:
                from_clause += ', ' + DI_TABLE_NAME[i] + " " + DI_SHORTCUT_NAME[i]

        # where
        where_statment = []
        if num_di_tables > 0:
            for i in range(len(index_table)):
                if index_table[i] == 1:
                    where_statment.append("{}.{}={}.{}".format(FACT_SHORTCUT_NAME, FACT_SCHEMA_NAME[i+1],
                        DI_SHORTCUT_NAME[i], DI_SCHEMA_NAME[i][0]))
        if num_di_tables > 1:
            reach_array = [0] * len(DI_SHORTCUT_NAME)
            max_clauses = num_di_tables - 1
            clause_num = 0
            while True:
                i = random.randint(0, len(index_table) - 1)
                j = random.randint(0, len(index_table) - 1)
                if clause_num == max_clauses or np.sum(reach_array) == num_di_tables:
                    break
                if i == j or (index_table[i] == 0 or index_table[j] == 0):
                    continue
                if reach_array[i] == 1 and reach_array[i] == reach_array[j]:
                    continue
                reach_array[i] = 1
                reach_array[j] = 1

                comp = random.choice(comp_symbols)
                where_statment.append("{}.{}{}{}.{}".format(DI_SHORTCUT_NAME[i], DI_SCHEMA_NAME[i][1], comp,
                    DI_SHORTCUT_NAME[j], DI_SCHEMA_NAME[j][1]))
                clause_num += 1
        where_clause = where_statment[0]
        for i in range(1, len(where_statment)):
            where_clause += ' AND ' + where_statment[i]

        # select
        select_clause = '*'
        f.write(template.format(select_clause, from_clause, where_clause)+'\n')

def create_dimension_table(size, minimum, maximum):
    table = []
    value_to_ids = {}
    current_size = 0
    for value in range(minimum, maximum + 1):
        value_to_ids[value] = []
    for i in range(size):
        table.append([])

    while True:
        for value in range(minimum, maximum + 1):
            if current_size == size:
                return table, value_to_ids
            key = -1
            while True:
                key = random.randint(0, size - 1)
                if len(table[key]) == 0:
                    break
            current_size += 1
            table[key].append(key)
            table[key].append(value)
            value_to_ids[value].append(key)
    assert(False)

def create_fact_table(size, di_tables, value_to_ids_tables, prob, minimum, maximum):
    table = []
    alpha = prob ** (1 / float(len(di_tables) - 1))
    for i in range(size):
        row = [i]
        value = random.randint(minimum, maximum)
        for table_index in range(len(di_tables)):
            if table_index == 0 or random.random() < alpha:
                key = random.choice(value_to_ids_tables[table_index][value])
                row.append(key)
                #row.append(value)
            else:
                while True:
                    key = random.randint(0, len(di_tables[table_index]) - 1)
                    new_value = di_tables[table_index][key][1]
                    if new_value != value:
                        row.append(key)
                        #row.append(new_value)
                        break
        table.append(row)
    return table

def write_human_readable_file(table, fileName):
    directory = 'input/db/data/'
    f = open(directory + fileName +"_humanreadable", "w+")
    for key in range(len(table)):
        tuple_output = ''
        for attribute in table[key]:
            tuple_output += str(attribute) + ','
        tuple_output = tuple_output[:-1] + '\n'
        f.write(tuple_output)

def write_binary_file(table, fileName):
    directory = 'input/db/data/'
    f = open(directory + fileName, "wb")
    page_size = 4096
    num_fields = len(table[0])
    total_tuples = len(table)
    tuples_per_page = (page_size - 8) / (num_fields * 4)
    remain_size_per_page = tuples_per_page * num_fields
    start_index = 0
    while total_tuples != 0:
        page = ''
        num_tuples = tuples_per_page if total_tuples > tuples_per_page else total_tuples
        end_index = start_index + num_tuples
        page += struct.pack('>I', num_fields)
        page += struct.pack('>I', num_tuples)
        for index in range(start_index, end_index):
            for entry in table[index]:
                page += struct.pack('>I', entry)
        for i in range((page_size - num_tuples * num_fields * 4 - 2 * 4) / 4):
            page += struct.pack('>I', 0)
        total_tuples -= num_tuples
        start_index = end_index
        f.write(page)

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('-p', '--prob', default= 0.5,
        help='the probability that all values are equal')
    parser.add_argument('-min', '--minimum', default= 0,
        help='the min value')
    parser.add_argument('-max', '--maximum', default= 50,
        help='the max value')
    parser.add_argument('-jt', '--join_table', default= 4,
        help='the number of join dimension tables in the query')
    parser.add_argument('-q', '--query_mode', default= 1,
        help='mode value should be either 1 or 2')
    args = parser.parse_args()
    prob = float(args.prob)
    print prob
    minimum = int(args.minimum)
    maximum = int(args.maximum)
    mode = int(args.query_mode)
    join_tables = int (args.join_table)
    assert(join_tables < 5)
    geneate_schema()
    if mode == 1:
        generate_queries1(num_di_tables = join_tables, min =  minimum, max = maximum, num_queries = 20)
    elif mode == 2:
        generate_queries2(num_di_tables = join_tables, num_queries = 20)
    di_tables = []
    value_to_ids_tables = []
    for size in DI_TABLE_SIZE:
        table, value_to_ids = create_dimension_table(size, minimum, maximum)
        di_tables.append(table)
        value_to_ids_tables.append(value_to_ids)

    fact_table = create_fact_table(FACT_TABLE_SIZE, di_tables, value_to_ids_tables, prob, minimum, maximum)

    for table_index in range(len(di_tables)):
        random.shuffle(di_tables[table_index])
        write_human_readable_file(di_tables[table_index], DI_TABLE_NAME[table_index])
        write_binary_file(di_tables[table_index], DI_TABLE_NAME[table_index])
    random.shuffle(fact_table)
    write_human_readable_file(fact_table, FACT_TABLE_NAME)
    write_binary_file(fact_table, FACT_TABLE_NAME)

if __name__== "__main__":
    random.seed(42)
    if not os.path.exists('input'):
        os.makedirs('input')
    if not os.path.exists('input/db'):
        os.makedirs('input/db')
    if not os.path.exists('input/db/data'):
        os.makedirs('input/db/data')
    if not os.path.exists('input/db/indexes'):
        os.makedirs('input/db/indexes')
    # f = open ('input/plan_builder_config.txt', 'w+')
    # f.write('0\n0\n1\n')
    # f.close()
    f = open('input/db/index_info.txt' ,'w+')
    f.close()

    main()
