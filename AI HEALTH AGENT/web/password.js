function validatePassword() {
    const password = document.getElementById('password').value;
    const confirm = document.getElementById('confirmPassword').value;
    const errorMsg = document.getElementById('errorMsg');
    const isValid = password.length >= 8
        && (password.match(/\d/g) || []).length >= 2
        && /[A-Z]/.test(password)
        && /[^A-Za-z0-9]/.test(password);

    if (!isValid) {
        errorMsg.textContent = 'Password must be 8+ characters with 2 numbers, 1 uppercase letter, and 1 special character.';
        return false;
    }

    if (password !== confirm) {
        errorMsg.textContent = 'Passwords do not match.';
        return false;
    }

    return true;
}
