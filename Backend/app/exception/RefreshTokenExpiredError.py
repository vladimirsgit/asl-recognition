class RefreshTokenExpiredError(Exception):
    def __init__(self, message = 'Refresh token expired.'):
        super().__init__(message)
        self.message = message