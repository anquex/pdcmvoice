clear all;
close all;
clc;

C=2.623;   % Average time to solve a node with MBT
theta=0.99; % Confidence
samples=300; % point to compute the solutions
f=inline('binocdf(k.*n.*p,n,p)-binocdf(ceil(n.*p./k)-1,n,p)-theta','k','n','p','theta');

batchsizes=[10, 100 1000];
Tmax=128; % max number of trials to refine GEGA estimate

Ts=[0:Tmax];

fractions=zeros(length(batchsizes),samples);
bestk=zeros(length(batchsizes),samples);
times=zeros(length(batchsizes),samples);

for i=1:length(batchsizes)
    n=batchsizes(i)
    %allowed time before considering the estimate
    GEGAtime=log2(n)+Tmax;
    fraction=GEGAtime/(n*C);
    if (fraction>1)
        % the time allowed to solve the problem is higher than the expected
        % running time to solve the whole batch. We expect the batch to be
        % solved in a smaller time.
        fraction=1;
    end
    tmp=linspace(0,fraction,samples+1);
    fractions(i,:)=tmp(2:end);
    clear tmp;
    for ii=1:size(fractions,2)
        p=fractions(i,ii);
        times(i,ii)=p*n*C;
        if (binocdf(0,n,p)>1-theta)
            bestk(i,ii)=NaN;
        else
            bestk(i,ii)=fzero(@(k) f(k,n,p,theta),1.5,{'Display','iter'});
        end
    end
end

figure;
hold all;
for i=1:length(batchsizes)
    plot(fractions(i,:),bestk(i,:));
end

figure;
hold all;
for i=1:length(batchsizes)
    plot(times(i,:),bestk(i,:));
end

% keep best
parsedbestk=bestk;
for i=1:length(batchsizes)
    for ii=1:size(fractions,2)-1
        if(parsedbestk(i,ii)<parsedbestk(i,ii+1))
            parsedbestk(i,ii+1)=parsedbestk(i,ii);
        end
    end
end

h={};
figure;
hold all;
for i=1:length(batchsizes)
    h{i}=plot(times(i,:),parsedbestk(i,:));
end


%GEGA
GEGAbestk=zeros(length(batchsizes),Tmax+1);
GEGAtimes=zeros(length(batchsizes),Tmax+1);
for i=1:length(batchsizes)
    n=batchsizes(i);
    GREENBERGtime=log2(n);
    for ii=1:length(Ts)
        T=Ts(ii);
        GEGAtimes(i,ii)=GREENBERGtime+T;
        load(['distributions/D-n-' int2str(n) '-T-' int2str(T)],'D');
        GEGAbestk(i,ii)=find_min_k_given_T_and_theta(n,D,theta);
    end
end

for i=1:length(batchsizes)
    h{i}=plot(GEGAtimes(i,:),GEGAbestk(i,:));
end

set(gca,'XLim',[0 128]);
set(gca,'YTick',[0:0.5:8])
legend('Cidon n=10','Cidon n=100','Cidon n=1000','GEGA n=10','GEGA n=100','GEGA n=1000')
xlabel('Elapsed time in slots');
ylabel('k accuracy');

fh = figure(3);
%saveas(fh, 'comparison-gega-cidon', 'epsc');