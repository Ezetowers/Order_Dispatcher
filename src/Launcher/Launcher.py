import subprocess
import sys
import os
import ConfigParser


class Launcher(object):
    def __init__(self):
        self._config = ConfigParser.RawConfigParser()
        self._config.read('launcher.ini')
        self._absolute_path = self._config.get("MAIN", "absolute-path")
        self._processes_pid_list = []
        self._processes_config_file = self._config.get("MAIN",
                                                       "processes-config-file")
        self._common_classpath = ""
        self._common_classpath = self.process_classpath("MAIN")

    def process_classpath(self, section):
        # Process the classpath
        classpath = self._config.get(section, "classpath")

        ret_classpath = ""
        for lib in classpath.split(":"):
            ret_classpath += self._absolute_path + lib + ":"
        return self._common_classpath + ret_classpath

    def init_system_processes(self):
        for section in self._config.sections():
            if section == "MAIN":
                continue

            if not self._config.getboolean(section, "run"):
                continue

            classpath = self.process_classpath(section)
            classname = self._config.get(section, "class-name")
            amount_processes = self._config.getint(section, "amount")

            for i in range(1, amount_processes + 1):
                print "Proceed to execute instance with ID {0} of program {1}"\
                    .format(str(i), classname.split(".")[0])

                call_args = []
                call_args.extend(["java",
                                  "-cp",
                                  classpath[:-1],
                                  classname,
                                  str(i),
                                  self._processes_config_file])
                process = subprocess.Popen(call_args, shell=False)
                if self._config.getboolean(section, "kill"):
                    self._processes_pid_list.append(process)

    def wait_for_events(self):
        # Wait for an input
        prompt = " Write 'STOP' to terminate the "\
                 "system. Write the section name "\
                 "of a process to run a instance of it.\n"

        while 1:
            user_input = raw_input(prompt)

            if user_input in self._config.sections():
                self.run_process(user_input)
            elif user_input == "STOP":
                self.kill_processes()
                break

    def run_process(self, section):
        classpath = self.process_classpath(section)
        classname = self._config.get(section, "class-name")
        print "Proceed to run program " + classname

        call_args = []
        call_args.extend(["java",
                          "-cp",
                          classpath[:-1],
                          classname,
                          "X",
                          self._processes_config_file])

        process = subprocess.Popen(call_args, shell=False)
        if self._config.getboolean(section, "kill"):
            self._processes_pid_list.append(process)

    def kill_processes(self):
        for process in self._processes_pid_list:
            # FIXME: This just work on Linux!!
            os.system("kill -15 " + str(process.pid))


def main():
    launcher = Launcher()
    launcher.init_system_processes()
    launcher.wait_for_events()


if __name__ == '__main__':
    main()