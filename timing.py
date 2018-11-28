from subprocess import *
import time

cmd = ['java', '-jar','cs4321_p2complete.jar', 'input', 'output', 'temp']
start_time = time.time()

#code here
call(cmd)
# process = Popen(['java', '-jar','cs4321_p2complete.jar', 'input', 'output', 'temp'], stdout=PIPE, stderr=PIPE)

print("--- %s seconds ---" % (time.time() - start_time))
