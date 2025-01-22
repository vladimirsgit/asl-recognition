class InvalidCodeError(Exception):
    def __init__(self, message = 'The code might have expired. Please navigate back and send a new Sign Up request or try again with a valid code.'):
        super().__init__(message)
        self.message = message