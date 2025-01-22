class EmailAlreadyExistsError(Exception):
    def __init__(self, message = 'Email already registered.'):
        super().__init__(message)
        self.message = message