import subprocess
import sys
import os
import ConfigParser

def main():
    config = ConfigParser.RawConfigParser()
    config.read('launcher.ini')
    absolutePath = config.get("MAIN", "absolute-path")
    processesConfigFile = config.get("MAIN", "processes-config-file")
    processList = []

    commonClasspath = processClasspath(absolutePath, 
                                       config.get("MAIN", "classpath"))

    for section in config.sections():
        if section == "MAIN":
            continue

        if not config.getboolean(section, "run")    :
            continue

        classpath = commonClasspath 
        classpath += processClasspath(absolutePath,
                                      config.get(section, "classpath"))

        className = config.get(section, "class-name")
        amountProcesses = config.getint(section, "amount")

        for i in range(1, amountProcesses + 1):
            print "Proceed to execute instance with ID {0} of program {1}"\
                .format(str(i), className.split(".")[0])
            callArgs = []
            callArgs.extend(["java", 
                             "-cp", 
                             classpath[:-1], 
                             className, 
                             str(i),
                             processesConfigFile])

            process = subprocess.Popen(callArgs, shell=False)
            if config.getboolean(section, "kill"):
                processList.append(process)

    # Wait for an input
    user_input = raw_input("Write 'Mondongo' to terminate the system: ")
    while user_input != "Mondongo":
        user_input = raw_input("Write 'Mondongo' to terminate the system: ")

    print "Proceed to terminate system..."
    for process in processList:
        # FIXME: This only work in Unix systems.
        os.system("kill -15 " + str(process.pid))

def processClasspath(absolutePath, classpath):
    # Process the classpath
    libs = classpath.replace(" ", "\ ").split(":")
    retClasspath = ""
    
    for lib in libs:
        retClasspath += absolutePath + lib + ":"
    return retClasspath

if __name__ == '__main__':
    main()