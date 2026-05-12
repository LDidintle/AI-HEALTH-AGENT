function validateResetPassword() {
    const password = document.getElementById('password').value;
    const confirmPassword = document.getElementById('confirmPassword').value;
    const error = document.getElementById('resetPasswordError');

    const strong = password.length >= 8
        && (password.match(/\d/g) || []).length >= 2
        && /[A-Z]/.test(password)
        && /[^A-Za-z0-9]/.test(password);

    if (!strong) {
        error.textContent = 'Password must be 8+ characters with 2 numbers, 1 uppercase letter, and 1 special character.';
        error.style.display = 'block';
        return false;
    }

    if (password !== confirmPassword) {
        error.textContent = 'Passwords do not match.';
        error.style.display = 'block';
        return false;
    }

    error.style.display = 'none';
    return true;
}
