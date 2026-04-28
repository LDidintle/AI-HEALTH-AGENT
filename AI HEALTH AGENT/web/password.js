function validatePassword() {
    const password = document.getElementById('password').value;
    const confirm = document.getElementById('confirmPassword').value;
    const errorMsg = document.getElementById('errorMsg');
    const isValid = password.length >= 7 && (password.match(/\d/g) || []).length >= 2 && /[A-Z]/.test(password);

    if (!isValid) {
        errorMsg.textContent = 'Password does not meet the requirements.';
        return false;
    }

    if (password !== confirm) {
        errorMsg.textContent = 'Passwords do not match.';
        return false;
    }

    return true;
}
