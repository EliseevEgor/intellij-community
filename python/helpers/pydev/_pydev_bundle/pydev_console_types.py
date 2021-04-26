import sys, traceback

err = False

def set_err(value):
    global err
    err = value

def get_err():
    return err

def info(type, value, tb):
    set_err(True)
    if (not sys.stderr.isatty() or
            not sys.stdin.isatty()):
        original_hook(type, value, tb)
    else:
        traceback.print_exception(type, value, tb)


original_hook = sys.excepthook
if sys.excepthook == sys.__excepthook__:

    sys.excepthook = info


class CodeFragment:
    def __init__(self, text, is_single_line=True):
        self.text = text
        self.is_single_line = is_single_line

    def append(self, code_fragment):
        self.text = self.text + "\n" + code_fragment.text
        if not code_fragment.is_single_line:
            self.is_single_line = False


class Command:
    def __init__(self, interpreter, code_fragment):
        """
        :type code_fragment: CodeFragment
        :type interpreter: InteractiveConsole
        """
        self.interpreter = interpreter
        self.code_fragment = code_fragment
        self.more = None


    def symbol_for_fragment(code_fragment):
        if code_fragment.is_single_line:
            symbol = 'single'
        else:
            symbol = 'exec' # Jython doesn't support this
        return symbol
    symbol_for_fragment = staticmethod(symbol_for_fragment)

    def run(self):
        text = self.code_fragment.text
        symbol = self.symbol_for_fragment(self.code_fragment)

        self.more = self.interpreter.runsource(text, '<input>', symbol)
