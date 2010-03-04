clear all;
close all;
clc;


k=2;
n=1000; % batch size
%p % initial batch split probability

% Binomial distribution with mean np
% Var np(1-p)
% behaves as a standar normal distribution when n -> inf
%P = normcdf(X,mu,sigma)

%mu=n.*p;
%sigma=sqrt(n.*p.*(1-p));
%x1=k.*n.*p;
%x2=n.*p./k;
%f=inline('normcdf(x1,mu,sigma)+1-normcdf(x2,mu,sigma)','x1','x2','mu','sigma');
%+0.5 for continuity correction

%Normal
fn=inline('normcdf(k.*n.*p+0.5,n.*p,sqrt(n.*p.*(1-p)))-normcdf(n.*p./k+0.5,n.*p,sqrt(n.*p.*(1-p)))-.99','k','n','p');

tol=1e-12;
p=bisect(2^-32,1-2^-32,fn,k,n,tol);
x=[0:0.005:1];
plot(x,fn(k,n,x));

disp(' ');
disp (['Taglia n           : ' num2str(n)]);
disp (['Errore k           : ' num2str(k)]);

disp(' ');
disp('--Normal Approx--')
disp (['Probabilità minima : ' num2str(p)]);
disp('Parametri di qualità');
disp(' ');
disp(['np      :' num2str(n.*p)]);
disp(['n(1-p)  :' num2str(n.*(1-p))]);
disp(['np(1-p) :' num2str(n.*p.*(1-p))]);

%Poisson
%lambda=n.*p;
fp=inline('poisscdf(k.*n.*p,n.*p)-poisscdf(n.*p./k,n.*p)-0.99','k','n','p');
p=bisect(2^-32,1-2^-32,fp,k,n,tol);

disp(' ');
disp('--Poisson Approx--')
disp (['Probabilità minima : ' num2str(p)]);
disp('Parametri di qualità');
disp(' ');
disp(['n: (>=20)    :' num2str(n) ' p:  (<=0.05)  :' num2str(p)]);
disp(['n: (>=100)   :' num2str(n) ' np:  (<=10)   :' num2str(n.*p)]);


%binomial
fb=inline('binocdf(k.*n.*p,n,p)-binocdf(n.*p./k,n,p)-0.99','k','n','p');
p=bisect(2^-32,1-2^-32,fb,k,n,tol);

disp(' ');
disp('--Exact Solution--')
disp (['Probabilità minima : ' num2str(p)]);



