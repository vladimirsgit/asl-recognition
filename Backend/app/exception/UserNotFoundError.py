class UserNotFoundError(Exception):
    def __init__(self, message = 'User not found.'):
        super().__init__(message)
        self.message = message