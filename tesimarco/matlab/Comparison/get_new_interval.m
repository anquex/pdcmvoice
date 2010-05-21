function [a1,b1] = get_new_interval(a0,b0,f,k,n)

%f('W','p','n')

% Helper function for method of bisection for solving f(x) = 0 for x.
%
% Given an initial interval [a0,b0] that satisfies f(a0)*f(b0) < 0, this
% function bisects the interval and returns [a1,b1], where a1 and b1 are
% determined so that f(a1)*f(b1) < 0.
%
% Variables:
%
% input:  a0  - left  endpoint of the initial interval
%         b0  - right endpoint of the initial interval
%         f   - handle to the function f
%
% output: a1  - left  endpoint of the new interval
%         b1  - right endpoint of the new interval
%
% local:  c0  - midpoint of the initial interval (i.e., [a0,b0])
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

% Compute the midpoint of the initial interval.
c0 = 0.5*(a0+b0);

% Determine the new, bisected interval.
if (f(k,n,c0)*f(k,n,b0) > 0.0)
  % xi is located in the interval [a0,c0].
  a1 = a0;
  b1 = c0;
else
  % xi is located in the interval [c0,b0].
  a1 = c0;
  b1 = b0;
end %if-else

return;
