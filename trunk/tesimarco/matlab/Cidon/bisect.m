function [x,num_its] = bisect(a0,b0,f,k,n,tol)
%f(k,n,p)
% Implements the method of bisection for solving f(x) = 0 for x.
%
% Given an initial interval [a0,b0], iteratively uses the helper function
% get_new_interval to solve approximately f(x) = 0 for x to the specified
% tolerance.
% 
% Variables:
%
% input:  a0  - left  endpoint of the initial interval
%         b0  - right endpoint of the initial interval
%         f   - handle to the function f
%         tol - error tolerance
%
% output: x   - the approximate solution to f(x) = 0
%         N   - the number of iterations required to attain the desired
%               tolerance on the approximate solution
%
% local:  ak  - left  endpoint of the current interval
%         bk  - right endpoint of the current interval
%
% NOTE: This function requires that f(a0)*f(b0) < 0, i.e. that f(a0) and
% f(b0) have opposite signs.  This function also requires that a0 < b0.  An
% error will result if either of these conditions are not satisfied on
% input.

% Check the error conditions.

if (f(k,n,a0)*f(k,n,b0) >= 0.0)
  error('f(k,n,a0)*f(k,n,b0) >= 0.0');
end %if

if (a0 >= b0)
  error('a0 >= b0');
end %if

% Initialize ak and bk to equal a0 and b0, respectively.
ak = a0;
bk = b0;

% Iteratively bisect the interval until a sufficiently accurate
% approximation to x is determined.  The approximate solution is taken to be
% the midpoint of the final interval.
%
% Along the way, we also keep track of the number of iterations performed.
%
% TO CONSIDER: Why do we iterate until (bk - ak) <= 2.0*tol rather than
% until (bk - ak) <= tol?
num_its = 0;
while ((bk - ak) > 2.0*tol)
  [ak,bk] = get_new_interval(ak,bk,f,k,n); % NOTE: This overwrites the current
    %disp(['Nuovo intervallo :' num2str(ak) ' ' num2str(bk)]);
				       % values of ak and bk with the new
				       % values of ak and bk.
  num_its = num_its + 1; % Update the iteration count.
end %while

% Compute the midpoint of the final interval.
x = 0.5*(ak+bk);

return;